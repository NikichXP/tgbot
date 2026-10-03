package com.nikichxp.tgbot.core.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object SecureTokens {

    private val random = SecureRandom()

    fun randomUrlSafe(byteCount: Int = 32): String {
        val bytes = ByteArray(byteCount).also { random.nextBytes(it) }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun sha256Hex(value: String): String =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }

    fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(), b.toByteArray())
}
