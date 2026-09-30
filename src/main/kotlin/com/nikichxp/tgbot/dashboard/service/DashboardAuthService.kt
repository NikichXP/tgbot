package com.nikichxp.tgbot.dashboard.service

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.service.TgBotV2Service
import com.nikichxp.tgbot.dashboard.connector.TelegramBotApiClient
import com.nikichxp.tgbot.dashboard.dto.DashboardPrincipal
import com.nikichxp.tgbot.dashboard.dto.DashboardUserDto
import com.nikichxp.tgbot.dashboard.dto.IssuedTokens
import com.nikichxp.tgbot.dashboard.dto.LoginResult
import com.nikichxp.tgbot.dashboard.entity.DashboardSession
import com.nikichxp.tgbot.dashboard.error.DashboardForbiddenException
import com.nikichxp.tgbot.dashboard.error.DashboardUnauthorizedException
import com.nikichxp.tgbot.dashboard.repository.DashboardAccessTokenRepository
import com.nikichxp.tgbot.dashboard.repository.DashboardSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64

@Service
class DashboardAuthService(
    private val appConfig: AppConfig,
    private val tgBotV2Service: TgBotV2Service,
    private val telegramBotApiClient: TelegramBotApiClient,
    private val sessionRepository: DashboardSessionRepository,
    private val accessTokenRepository: DashboardAccessTokenRepository
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val random = SecureRandom()

    @Volatile
    private var cachedLoginBotUsername: String? = null

    private val accessTokenTtl: Duration
        get() = Duration.ofMinutes(appConfig.dashboard.accessTokenTtlMinutes)

    private fun loginBotName(): String =
        appConfig.dashboard.loginBot?.takeIf { it.isNotBlank() }
            ?: appConfig.adminBot?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Neither app.dashboard.loginBot nor app.adminBot is configured")

    private suspend fun loginBotToken(): String = withContext(Dispatchers.IO) {
        tgBotV2Service.getTokenById(loginBotName())
    }

    suspend fun loginBotUsername(): String {
        cachedLoginBotUsername?.let { return it }
        val identity = telegramBotApiClient.getMe(loginBotToken())
            ?: throw IllegalStateException("Login bot token is invalid")
        return identity.username.also { cachedLoginBotUsername = it }
    }

    suspend fun loginWithTelegram(payload: Map<String, Any?>, userAgent: String?): LoginResult {
        val user = TelegramLoginVerifier.verify(payload, loginBotToken())
            ?: throw DashboardUnauthorizedException("Invalid Telegram login data")
        if (user.id != appConfig.adminId) {
            logger.warn("Dashboard login rejected for non-admin telegram user ${user.id} (@${user.username})")
            throw DashboardForbiddenException("Only the bot admin may use the dashboard")
        }

        val refreshToken = randomToken()
        val now = Instant.now()
        val session = sessionRepository.insert(
            DashboardSession(
                refreshTokenHash = sha256(refreshToken),
                userId = user.id,
                firstName = user.firstName,
                username = user.username,
                photoUrl = user.photoUrl,
                userAgent = userAgent?.take(300),
                createdAt = now,
                lastUsedAt = now
            )
        )
        logger.info("Dashboard login for user ${user.id}")

        val accessToken = issueAccessToken(session)
        return LoginResult(IssuedTokens(accessToken, refreshToken, accessTokenTtl), session.toUserDto())
    }

    suspend fun refresh(refreshToken: String): Pair<String, Duration> {
        val session = sessionRepository.findById(sha256(refreshToken))
            ?: throw DashboardUnauthorizedException("Unknown refresh token")
        if (session.userId != appConfig.adminId) {
            // admin was changed since this session was created
            sessionRepository.deleteById(session.refreshTokenHash)
            throw DashboardForbiddenException("Only the bot admin may use the dashboard")
        }
        session.lastUsedAt = Instant.now()
        sessionRepository.save(session)
        return issueAccessToken(session) to accessTokenTtl
    }

    suspend fun authenticate(accessToken: String): DashboardPrincipal {
        val principal = accessTokenRepository.find(sha256(accessToken))
            ?: throw DashboardUnauthorizedException("Access token is invalid or expired")
        if (principal.userId != appConfig.adminId) throw DashboardForbiddenException()
        return principal
    }

    suspend fun currentUser(principal: DashboardPrincipal): DashboardUserDto =
        sessionRepository.findById(principal.sessionId)?.toUserDto()
            ?: DashboardUserDto(principal.userId, null, null, null)

    suspend fun logout(refreshToken: String?, accessToken: String?) {
        accessToken?.let { accessTokenRepository.delete(sha256(it)) }
        refreshToken?.let { sessionRepository.deleteById(sha256(it)) }
    }

    private suspend fun issueAccessToken(session: DashboardSession): String {
        val token = randomToken()
        accessTokenRepository.save(
            sha256(token),
            DashboardPrincipal(session.userId, session.refreshTokenHash),
            accessTokenTtl
        )
        return token
    }

    private fun DashboardSession.toUserDto() = DashboardUserDto(userId, firstName, username, photoUrl)

    private fun randomToken(): String {
        val bytes = ByteArray(32).also { random.nextBytes(it) }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
