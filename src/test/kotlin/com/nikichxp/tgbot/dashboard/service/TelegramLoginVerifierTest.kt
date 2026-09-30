package com.nikichxp.tgbot.dashboard.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.security.MessageDigest
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class TelegramLoginVerifierTest {

    private val botToken = "123456:TEST-token"
    private val now = Instant.parse("2026-09-30T12:00:00Z")

    private fun sign(fields: Map<String, Any>): Map<String, Any?> {
        val dataCheckString = fields.toSortedMap().entries.joinToString("\n") { "${it.key}=${it.value}" }
        val secret = MessageDigest.getInstance("SHA-256").digest(botToken.toByteArray())
        val mac = Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(secret, "HmacSHA256")) }
        val hash = mac.doFinal(dataCheckString.toByteArray()).joinToString("") { "%02x".format(it) }
        return fields + ("hash" to hash)
    }

    private val fields = mapOf<String, Any>(
        "id" to 34080460L,
        "first_name" to "Nik",
        "username" to "nikichxp",
        "auth_date" to now.epochSecond - 60
    )

    @Test
    fun `accepts correctly signed payload`() {
        val user = TelegramLoginVerifier.verify(sign(fields), botToken, now)
        assertThat(user?.id).isEqualTo(34080460L)
        assertThat(user?.username).isEqualTo("nikichxp")
    }

    @Test
    fun `rejects tampered payload`() {
        val tampered = sign(fields) + ("id" to 1L)
        assertThat(TelegramLoginVerifier.verify(tampered, botToken, now)).isNull()
    }

    @Test
    fun `rejects payload signed with another bot token`() {
        assertThat(TelegramLoginVerifier.verify(sign(fields), "999:other", now)).isNull()
    }

    @Test
    fun `rejects stale payload`() {
        val stale = sign(fields + ("auth_date" to now.epochSecond - 2 * 24 * 3600))
        assertThat(TelegramLoginVerifier.verify(stale, botToken, now)).isNull()
    }

    @Test
    fun `rejects payload without hash`() {
        assertThat(TelegramLoginVerifier.verify(fields, botToken, now)).isNull()
    }
}
