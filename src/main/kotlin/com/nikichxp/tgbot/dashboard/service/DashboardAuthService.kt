package com.nikichxp.tgbot.dashboard.service

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.dashboard.dto.DashboardPrincipal
import com.nikichxp.tgbot.dashboard.dto.DashboardUserDto
import com.nikichxp.tgbot.dashboard.dto.IssuedTokens
import com.nikichxp.tgbot.dashboard.dto.LoginResult
import com.nikichxp.tgbot.dashboard.entity.DashboardSession
import com.nikichxp.tgbot.dashboard.error.DashboardForbiddenException
import com.nikichxp.tgbot.dashboard.error.DashboardUnauthorizedException
import com.nikichxp.tgbot.dashboard.repository.DashboardAccessTokenRepository
import com.nikichxp.tgbot.dashboard.repository.DashboardLoginNonceRepository
import com.nikichxp.tgbot.dashboard.repository.DashboardSessionRepository
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
    private val idTokenVerifier: TelegramIdTokenVerifier,
    private val sessionRepository: DashboardSessionRepository,
    private val accessTokenRepository: DashboardAccessTokenRepository,
    private val nonceRepository: DashboardLoginNonceRepository
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val random = SecureRandom()

    private val accessTokenTtl: Duration
        get() = Duration.ofMinutes(appConfig.dashboard.accessTokenTtlMinutes)

    fun telegramClientId(): String =
        appConfig.dashboard.telegramClientId?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("app.dashboard.telegramClientId is not configured")

    suspend fun issueLoginNonce(): String = randomToken().also { nonceRepository.save(it, NONCE_TTL) }

    suspend fun loginWithTelegram(idToken: String, userAgent: String?): LoginResult {
        val user = idTokenVerifier.verify(idToken, telegramClientId())
            ?: throw DashboardUnauthorizedException("Invalid Telegram login")
        if (!consumeIssuedNonce(user.nonce)) {
            throw DashboardUnauthorizedException("Login expired, please try again")
        }
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
                name = user.name,
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

    private suspend fun consumeIssuedNonce(nonce: String?): Boolean =
        nonce != null && nonceRepository.consumeIfPresent(nonce)

    private suspend fun issueAccessToken(session: DashboardSession): String {
        val token = randomToken()
        accessTokenRepository.save(
            sha256(token),
            DashboardPrincipal(session.userId, session.refreshTokenHash),
            accessTokenTtl
        )
        return token
    }

    private fun DashboardSession.toUserDto() = DashboardUserDto(userId, name, username, photoUrl)

    private fun randomToken(): String {
        val bytes = ByteArray(32).also { random.nextBytes(it) }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }

    companion object {
        private val NONCE_TTL: Duration = Duration.ofMinutes(10)
    }
}
