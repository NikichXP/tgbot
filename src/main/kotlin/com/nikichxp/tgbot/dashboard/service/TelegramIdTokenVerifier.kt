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

@Service
class TelegramIdTokenVerifier(
    private val telegramJwkSource: JWKSource<SecurityContext>
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun verify(idToken: String, clientId: String): TelegramIdTokenUser? {
        val processor = DefaultJWTProcessor<SecurityContext>().apply {
            jwsKeySelector = JWSVerificationKeySelector(SUPPORTED_BOTFATHER_SIGNING_ALGORITHMS, telegramJwkSource)
            jwtClaimsSetVerifier = DefaultJWTClaimsVerifier(
                clientId,
                JWTClaimsSet.Builder().issuer(TelegramOidcConfig.ISSUER).build(),
                setOf("sub", "exp", "iat", "id")
            )
        }
        val claims = try {
            withContext(Dispatchers.IO) { processor.process(idToken, null) }
        } catch (e: Exception) {
            logger.warn("Telegram id_token rejected: ${e.message}")
            return null
        }
        return try {
            TelegramIdTokenUser(
                id = parseTelegramUserIdFromNumberOrString(claims.getClaim("id")),
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

    private fun parseTelegramUserIdFromNumberOrString(claim: Any?): Long = when (claim) {
        is Number -> claim.toLong()
        is String -> claim.toLong()
        else -> throw IllegalArgumentException("\"id\" claim is missing or not numeric: $claim")
    }

    companion object {
        private val SUPPORTED_BOTFATHER_SIGNING_ALGORITHMS = setOf(JWSAlgorithm.RS256, JWSAlgorithm.ES256)
    }
}
