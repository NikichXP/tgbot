package com.nikichxp.tgbot.dashboard.service

import com.nikichxp.tgbot.dashboard.dto.TelegramLoginUser
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object TelegramLoginVerifier {

    val MAX_AUTH_AGE: Duration = Duration.ofDays(1)

    fun verify(payload: Map<String, Any?>, botToken: String, now: Instant = Instant.now()): TelegramLoginUser? {
        val fields = payload.filterValues { it != null }.mapValues { it.value.toString() }
        val hash = fields["hash"] ?: return null

        val dataCheckString = fields
            .filterKeys { it != "hash" }
            .toSortedMap()
            .entries
            .joinToString("\n") { "${it.key}=${it.value}" }

        val secretKey = MessageDigest.getInstance("SHA-256").digest(botToken.toByteArray())
        val mac = Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(secretKey, "HmacSHA256")) }
        val expected = mac.doFinal(dataCheckString.toByteArray()).toHex()
        if (!MessageDigest.isEqual(expected.toByteArray(), hash.lowercase().toByteArray())) return null

        val authDate = fields["auth_date"]?.toLongOrNull() ?: return null
        if (Duration.between(Instant.ofEpochSecond(authDate), now) > MAX_AUTH_AGE) return null

        return TelegramLoginUser(
            id = fields["id"]?.toLongOrNull() ?: return null,
            firstName = fields["first_name"],
            lastName = fields["last_name"],
            username = fields["username"],
            photoUrl = fields["photo_url"]
        )
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
}
