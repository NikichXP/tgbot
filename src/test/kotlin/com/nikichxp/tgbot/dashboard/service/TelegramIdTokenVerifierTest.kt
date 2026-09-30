package com.nikichxp.tgbot.dashboard.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Date

class TelegramIdTokenVerifierTest {

    private val clientId = "360838312"
    private val telegramKey: RSAKey = RSAKeyGenerator(2048).keyID("oidc-1").generate()
    private val verifier = TelegramIdTokenVerifier(ImmutableJWKSet<SecurityContext>(JWKSet(telegramKey.toPublicJWK())))

    private fun token(
        key: RSAKey = telegramKey,
        issuer: String = "https://oauth.telegram.org",
        audience: String = clientId,
        expiresAt: Instant = Instant.now().plusSeconds(3600),
        id: Any? = 34080460L
    ): String {
        val claims = JWTClaimsSet.Builder()
            .issuer(issuer)
            .audience(audience)
            .subject("1234123412341234123")
            .issueTime(Date())
            .expirationTime(Date.from(expiresAt))
            .claim("name", "Nik")
            .claim("preferred_username", "nikichxp")
            .claim("nonce", "n-1")
            .apply { if (id != null) claim("id", id) }
            .build()
        return SignedJWT(JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.keyID).build(), claims)
            .apply { sign(RSASSASigner(key)) }
            .serialize()
    }

    private fun verify(idToken: String) = runBlocking { verifier.verify(idToken, clientId) }

    @Test
    fun `accepts valid token and extracts telegram user`() {
        val user = verify(token())
        assertThat(user?.id).isEqualTo(34080460L)
        assertThat(user?.username).isEqualTo("nikichxp")
        assertThat(user?.name).isEqualTo("Nik")
        assertThat(user?.nonce).isEqualTo("n-1")
    }

    @Test
    fun `accepts telegram user id sent as a string`() {
        assertThat(verify(token(id = "34080460"))?.id).isEqualTo(34080460L)
    }

    @Test
    fun `rejects non-numeric telegram user id`() {
        assertThat(verify(token(id = "abc"))).isNull()
    }

    @Test
    fun `rejects token signed by another key`() {
        val foreignKey = RSAKeyGenerator(2048).keyID("oidc-1").generate()
        assertThat(verify(token(key = foreignKey))).isNull()
    }

    @Test
    fun `rejects token for another client`() {
        assertThat(verify(token(audience = "111"))).isNull()
    }

    @Test
    fun `rejects token from another issuer`() {
        assertThat(verify(token(issuer = "https://evil.example"))).isNull()
    }

    @Test
    fun `rejects expired token`() {
        assertThat(verify(token(expiresAt = Instant.now().minusSeconds(3600)))).isNull()
    }

    @Test
    fun `rejects token without telegram user id`() {
        assertThat(verify(token(id = null))).isNull()
    }

    @Test
    fun `rejects tampered token`() {
        val parts = token().split(".")
        val tamperedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString(
                String(java.util.Base64.getUrlDecoder().decode(parts[1])).replace("34080460", "1").toByteArray()
            )
        assertThat(verify("${parts[0]}.$tamperedPayload.${parts[2]}")).isNull()
    }

    @Test
    fun `rejects garbage`() {
        assertThat(verify("not-a-jwt")).isNull()
    }
}
