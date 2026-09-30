package com.nikichxp.tgbot.dashboard.service

import com.nikichxp.tgbot.dashboard.config.TelegramOidcConfig
import com.nikichxp.tgbot.dashboard.dto.TelegramIdTokenUser
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Verifies an `id_token` issued by "Log In with Telegram" (OpenID Connect):
 * signature against Telegram's JWKS, `iss`, `aud` (= our Client ID) and `exp`.
 * https://core.telegram.org/bots/telegram-login
 */
@Service
class TelegramIdTokenVerifier(private val telegramJwkSource: JWKSource<SecurityContext>) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun verify(idToken: String, clientId: String): TelegramIdTokenUser? {
        val processor = DefaultJWTProcessor<SecurityContext>().apply {
            jwsKeySelector = JWSVerificationKeySelector(ALLOWED_ALGORITHMS, telegramJwkSource)
            jwtClaimsSetVerifier = DefaultJWTClaimsVerifier(
                clientId,
                JWTClaimsSet.Builder().issuer(TelegramOidcConfig.ISSUER).build(),
                setOf("sub", "exp", "iat", "id")
            )
        }
        val claims = try {
            // JWKS may be fetched over (blocking) HTTP on a cache miss
            withContext(Dispatchers.IO) { processor.process(idToken, null) }
        } catch (e: Exception) {
            logger.warn("Telegram id_token rejected: ${e.message}")
            return null
        }
        return try {
            TelegramIdTokenUser(
                id = telegramUserId(claims.getClaim("id")),
                name = claims.getStringClaim("name"),
                username = claims.getStringClaim("preferred_username"),
                photoUrl = claims.getStringClaim("picture"),
                nonce = claims.getStringClaim("nonce")
            )
        } catch (e: Exception) {
            logger.warn("Telegram id_token has unexpected claims: ${e.message}")
            null
        }
    }

    // Telegram sends the numeric user id as a JSON string (despite the docs example showing a number)
    private fun telegramUserId(claim: Any?): Long = when (claim) {
        is Number -> claim.toLong()
        is String -> claim.toLong()
        else -> throw IllegalArgumentException("\"id\" claim is missing or not numeric: $claim")
    }

    companion object {
        // the algorithm is chosen per bot in @BotFather; EdDSA/ES256K would need extra crypto providers
        private val ALLOWED_ALGORITHMS = setOf(JWSAlgorithm.RS256, JWSAlgorithm.ES256)
    }
}
