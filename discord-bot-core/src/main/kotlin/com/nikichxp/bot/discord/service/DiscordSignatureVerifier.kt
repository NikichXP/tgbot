package com.nikichxp.bot.discord.service

import com.nikichxp.bot.common.config.AppConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.HexFormat

@Service
class DiscordSignatureVerifier(
    private val appConfig: AppConfig
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun verifySignature(signature: String?, timestamp: String?, body: String): Boolean {
        val publicKeyHex = appConfig.discord.publicKey?.takeIf { it.isNotBlank() } ?: return true
        if (signature == null || timestamp == null) return false

        return try {
            val verifier = Signature.getInstance(ED25519)
            verifier.initVerify(toPublicKey(publicKeyHex))
            verifier.update((timestamp + body).toByteArray())
            verifier.verify(HexFormat.of().parseHex(signature))
        } catch (e: Exception) {
            logger.error("Signature verification failed", e)
            false
        }
    }

    private fun toPublicKey(publicKeyHex: String) =
        KeyFactory.getInstance(ED25519).generatePublic(
            X509EncodedKeySpec(ED25519_X509_PREFIX + HexFormat.of().parseHex(publicKeyHex))
        )

    private companion object {
        const val ED25519 = "Ed25519"
        val ED25519_X509_PREFIX: ByteArray = HexFormat.of().parseHex("302a300506032b6570032100")
    }
}
