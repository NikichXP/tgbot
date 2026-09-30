package com.nikichxp.tgbot.oauth.service

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.util.SecureTokens
import com.nikichxp.tgbot.dashboard.service.TelegramIdTokenVerifier
import com.nikichxp.tgbot.oauth.dto.OAuthAuthorizationGrant
import com.nikichxp.tgbot.oauth.dto.OAuthAuthorizePage
import com.nikichxp.tgbot.oauth.dto.OAuthLoginCompletion
import com.nikichxp.tgbot.oauth.dto.OAuthLoginRequest
import com.nikichxp.tgbot.oauth.dto.OAuthResponseMode
import com.nikichxp.tgbot.oauth.dto.OAuthTokenRequest
import com.nikichxp.tgbot.oauth.dto.OAuthTokenResponse
import com.nikichxp.tgbot.oauth.dto.OAuthUserDto
import com.nikichxp.tgbot.oauth.error.OAuthException
import com.nikichxp.tgbot.oauth.repository.OAuthAuthorizationCodeRepository
import com.nikichxp.tgbot.oauth.repository.OAuthClientRepository
import com.nikichxp.tgbot.oauth.repository.OAuthLoginRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class OAuthAuthorizationService(
    private val appConfig: AppConfig,
    private val idTokenVerifier: TelegramIdTokenVerifier,
    private val clientRepository: OAuthClientRepository,
    private val loginRequestRepository: OAuthLoginRequestRepository,
    private val codeRepository: OAuthAuthorizationCodeRepository
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun startAuthorization(
        clientId: String?,
        redirectUri: String?,
        state: String?,
        responseMode: String?
    ): OAuthAuthorizePage {
        val client = clientId?.let { clientRepository.findById(it) }
            ?: throw OAuthException.invalidRequest("Unknown client_id")
        if (redirectUri == null || redirectUri !in client.redirectUris) {
            throw OAuthException.invalidRequest("redirect_uri is not registered for this client")
        }
        val mode = OAuthResponseMode.fromWireNameOrDefault(responseMode)
            ?: throw OAuthException.invalidRequest("Unsupported response_mode")
        if (state != null && state.length > MAX_STATE_LENGTH) {
            throw OAuthException.invalidRequest("state is too long")
        }

        val nonce = SecureTokens.randomUrlSafe()
        loginRequestRepository.save(nonce, OAuthLoginRequest(client.clientId, redirectUri, state, mode), LOGIN_REQUEST_TTL)
        return OAuthAuthorizePage(
            telegramClientId = telegramClientId(),
            nonce = nonce,
            clientName = client.name,
            responseMode = mode,
            frameAncestors = client.redirectUris.map(RedirectUriPolicy::originOf).distinct()
        )
    }

    suspend fun completeLogin(idToken: String): OAuthLoginCompletion {
        val user = idTokenVerifier.verify(idToken, telegramClientId())
            ?: throw OAuthException.accessDenied("Invalid Telegram login")
        val request = user.nonce?.let { loginRequestRepository.consume(it) }
            ?: throw OAuthException.accessDenied("Login expired, please try again")

        val code = SecureTokens.randomUrlSafe()
        codeRepository.save(
            SecureTokens.sha256Hex(code),
            OAuthAuthorizationGrant(
                clientId = request.clientId,
                redirectUri = request.redirectUri,
                user = OAuthUserDto(user.id, user.name, user.username, user.photoUrl),
                authTime = Instant.now()
            ),
            AUTHORIZATION_CODE_TTL
        )
        logger.info("OAuth: telegram user ${user.id} authorized for client ${request.clientId}")
        return OAuthLoginCompletion(
            responseMode = request.responseMode,
            redirectUri = request.redirectUri,
            targetOrigin = RedirectUriPolicy.originOf(request.redirectUri),
            code = code,
            state = request.state
        )
    }

    suspend fun exchangeCode(request: OAuthTokenRequest): OAuthTokenResponse {
        if (request.grantType != AUTHORIZATION_CODE_GRANT) throw OAuthException.unsupportedGrantType()
        val client = request.clientId?.let { clientRepository.findById(it) } ?: throw OAuthException.invalidClient()
        val secret = request.clientSecret ?: throw OAuthException.invalidClient()
        if (!SecureTokens.constantTimeEquals(SecureTokens.sha256Hex(secret), client.secretHash)) {
            throw OAuthException.invalidClient()
        }
        val code = request.code ?: throw OAuthException.invalidRequest("code is required")
        val redirectUri = request.redirectUri ?: throw OAuthException.invalidRequest("redirect_uri is required")

        val grant = codeRepository.consume(SecureTokens.sha256Hex(code))
            ?: throw OAuthException.invalidGrant("Authorization code is invalid, expired or already used")
        if (grant.clientId != client.clientId) {
            throw OAuthException.invalidGrant("Authorization code was issued to another client")
        }
        if (grant.redirectUri != redirectUri) {
            throw OAuthException.invalidGrant("redirect_uri does not match the authorization request")
        }
        return OAuthTokenResponse(grant.user, grant.authTime.epochSecond)
    }

    private fun telegramClientId(): String =
        appConfig.dashboard.telegramClientId?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("app.dashboard.telegramClientId is not configured")

    companion object {
        const val AUTHORIZATION_CODE_GRANT = "authorization_code"
        const val MAX_STATE_LENGTH = 512
        val LOGIN_REQUEST_TTL: Duration = Duration.ofMinutes(15)
        val AUTHORIZATION_CODE_TTL: Duration = Duration.ofSeconds(60)
    }
}
