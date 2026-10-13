package com.nikichxp.bot.discord.service

import com.nikichxp.bot.common.config.AppConfig
import org.junit.jupiter.api.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.util.HexFormat
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiscordSignatureVerifierTest {

    private val keyPair: KeyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair()
    private val rawPublicKeyHex = HexFormat.of().formatHex(keyPair.public.encoded.takeLast(32).toByteArray())
    private val verifier = DiscordSignatureVerifier(configWithPublicKey(rawPublicKeyHex))

    private val timestamp = "1760000000"
    private val body = """{"type":1}"""

    @Test
    fun `accepts body signed with the configured key`() {
        assertTrue(verifier.verifySignature(sign(timestamp + body), timestamp, body))
    }

    @Test
    fun `rejects tampered body`() {
        assertFalse(verifier.verifySignature(sign(timestamp + body), timestamp, """{"type":2}"""))
    }

    @Test
    fun `rejects request without signature headers`() {
        assertFalse(verifier.verifySignature(null, timestamp, body))
        assertFalse(verifier.verifySignature(sign(timestamp + body), null, body))
    }

    @Test
    fun `rejects malformed signature`() {
        assertFalse(verifier.verifySignature("not-hex", timestamp, body))
    }

    @Test
    fun `accepts any request when public key is not configured`() {
        val unconfiguredVerifier = DiscordSignatureVerifier(configWithPublicKey(null))

        assertTrue(unconfiguredVerifier.verifySignature(null, null, body))
    }

    private fun sign(payload: String): String {
        val signer = Signature.getInstance("Ed25519")
        signer.initSign(keyPair.private)
        signer.update(payload.toByteArray())
        return HexFormat.of().formatHex(signer.sign())
    }

    private fun configWithPublicKey(publicKey: String?) =
        AppConfig().apply { discord = AppConfig.Companion.Discord(publicKey = publicKey) }
}
