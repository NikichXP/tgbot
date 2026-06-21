package com.nikichxp.tgbot.discord

import com.nikichxp.tgbot.core.config.AppConfig
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Service
class DiscordService(
    private val appConfig: AppConfig,
    private val inputJsonStorage: InputJsonStorage
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun handleInteraction(
        signature: String?,
        timestamp: String?,
        bodyString: String,
        token: String
    ): InteractionResponse {
        if (!verifySignature(signature, timestamp, bodyString)) {
            return InteractionResponse.Unauthenticated
        }
        val body = Document.parse(bodyString)
        inputJsonStorage.saveJson(body.toJson(), token)
        return if (body["type"] == 1) {
            InteractionResponse.Pong
        } else {
            InteractionResponse.Ok
        }
    }

    fun verifySignature(signature: String?, timestamp: String?, body: String): Boolean {
        val publicKeyHex = appConfig.discord.publicKey ?: return true
        if (signature == null || timestamp == null) return false

        return try {
            val keyFactory = KeyFactory.getInstance("Ed25519")
            val publicKeyBytes = HexFormat.of().parseHex(publicKeyHex)
            
            // X.509 prefix for Ed25519 public key
            val x509Prefix = HexFormat.of().parseHex("302a300506032b6570032100")
            val combinedKey = ByteArray(x509Prefix.size + publicKeyBytes.size)
            System.arraycopy(x509Prefix, 0, combinedKey, 0, x509Prefix.size)
            System.arraycopy(publicKeyBytes, 0, combinedKey, x509Prefix.size, publicKeyBytes.size)
            
            val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(combinedKey))
            
            val sig = Signature.getInstance("Ed25519")
            sig.initVerify(publicKey)
            sig.update((timestamp + body).toByteArray())
            sig.verify(HexFormat.of().parseHex(signature))
        } catch (e: Exception) {
            logger.error("Signature verification failed", e)
            false
        }
    }
}

sealed class InteractionResponse {
    object Unauthenticated : InteractionResponse()
    object Pong : InteractionResponse()
    object Ok : InteractionResponse()
}
