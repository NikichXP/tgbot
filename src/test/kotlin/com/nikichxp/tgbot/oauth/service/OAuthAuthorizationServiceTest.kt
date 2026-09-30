package com.nikichxp.tgbot.oauth.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.util.SecureTokens
import com.nikichxp.tgbot.dashboard.dto.TelegramIdTokenUser
import com.nikichxp.tgbot.dashboard.service.TelegramIdTokenVerifier
import com.nikichxp.tgbot.oauth.dto.OAuthAuthorizationGrant
import com.nikichxp.tgbot.oauth.dto.OAuthLoginRequest
import com.nikichxp.tgbot.oauth.dto.OAuthResponseMode
import com.nikichxp.tgbot.oauth.dto.OAuthTokenRequest
import com.nikichxp.tgbot.oauth.entity.OAuthClient
import com.nikichxp.tgbot.oauth.error.OAuthException
import com.nikichxp.tgbot.oauth.repository.OAuthAuthorizationCodeRepository
import com.nikichxp.tgbot.oauth.repository.OAuthClientRepository
import com.nikichxp.tgbot.oauth.repository.OAuthLoginRequestRepository
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import java.time.Duration
import java.time.Instant

class OAuthAuthorizationServiceTest {

    private val clientSecret = "pancake-secret"
    private val redirectUri = "https://pancakes.example.com/auth/callback"
    private val pancakeClient = OAuthClient(
        clientId = "pancakes",
        name = "Pancake Club",
        secretHash = SecureTokens.sha256Hex(clientSecret),
        redirectUris = listOf(redirectUri, "http://localhost:5173/auth/callback"),
        createdAt = Instant.now()
    )

    private val clients = mutableMapOf(pancakeClient.clientId to pancakeClient)
    private val loginRequests = mutableMapOf<String, OAuthLoginRequest>()
    private val codes = mutableMapOf<String, OAuthAuthorizationGrant>()
    private val tokensByIdToken = mutableMapOf<String, TelegramIdTokenUser>()

    private val clientRepository = object : OAuthClientRepository(mock(MongoTemplate::class.java)) {
        override suspend fun findById(clientId: String) = clients[clientId]
    }

    private val loginRequestRepository =
        object : OAuthLoginRequestRepository(mock(ReactiveStringRedisTemplate::class.java), jacksonObjectMapper()) {
            override suspend fun save(nonce: String, request: OAuthLoginRequest, ttl: Duration) {
                loginRequests[nonce] = request
            }

            override suspend fun consume(nonce: String) = loginRequests.remove(nonce)
        }

    private val codeRepository =
        object : OAuthAuthorizationCodeRepository(mock(ReactiveStringRedisTemplate::class.java), jacksonObjectMapper()) {
            override suspend fun save(codeHash: String, grant: OAuthAuthorizationGrant, ttl: Duration) {
                codes[codeHash] = grant
            }

            override suspend fun consume(codeHash: String) = codes.remove(codeHash)
        }

    private val idTokenVerifier = object : TelegramIdTokenVerifier(ImmutableJWKSet<SecurityContext>(JWKSet())) {
        override suspend fun verify(idToken: String, clientId: String) = tokensByIdToken[idToken]
    }

    private val service = OAuthAuthorizationService(
        AppConfig(dashboard = AppConfig.Companion.Dashboard(telegramClientId = "360838312")),
        idTokenVerifier,
        clientRepository,
        loginRequestRepository,
        codeRepository
    )

    private fun telegramLogin(nonce: String?): String {
        val idToken = "id-token-${tokensByIdToken.size}"
        tokensByIdToken[idToken] = TelegramIdTokenUser(34080460L, "Nik", "nikichxp", null, nonce)
        return idToken
    }

    private fun tokenRequest(
        code: String?,
        clientId: String = "pancakes",
        secret: String = clientSecret,
        redirect: String = redirectUri,
        grantType: String = "authorization_code"
    ) = OAuthTokenRequest(grantType, code, redirect, clientId, secret)

    private fun authorizeAndLogin(state: String? = "st-1"): String = runBlocking {
        val page = service.startAuthorization("pancakes", redirectUri, state, "web_message")
        service.completeLogin(telegramLogin(page.nonce)).code
    }

    @Test
    fun `full flow hands the verified telegram user to the client backend`() = runBlocking<Unit> {
        val page = service.startAuthorization("pancakes", redirectUri, "st-1", "web_message")
        assertThat(page.telegramClientId).isEqualTo("360838312")
        assertThat(page.clientName).isEqualTo("Pancake Club")
        assertThat(page.frameAncestors).containsExactly("https://pancakes.example.com", "http://localhost:5173")

        val completion = service.completeLogin(telegramLogin(page.nonce))
        assertThat(completion.responseMode).isEqualTo(OAuthResponseMode.WEB_MESSAGE)
        assertThat(completion.targetOrigin).isEqualTo("https://pancakes.example.com")
        assertThat(completion.state).isEqualTo("st-1")

        val response = service.exchangeCode(tokenRequest(completion.code))
        assertThat(response.user.id).isEqualTo(34080460L)
        assertThat(response.user.username).isEqualTo("nikichxp")
    }

    @Test
    fun `response mode defaults to query redirect`() = runBlocking<Unit> {
        assertThat(service.startAuthorization("pancakes", redirectUri, null, null).responseMode)
            .isEqualTo(OAuthResponseMode.QUERY)
    }

    @Test
    fun `rejects unknown client, unregistered redirect uri and unknown response mode`() {
        runBlocking {
            assertThrows<OAuthException> { service.startAuthorization("nope", redirectUri, null, null) }
            assertThrows<OAuthException> { service.startAuthorization("pancakes", "https://evil.example/cb", null, null) }
            assertThrows<OAuthException> { service.startAuthorization("pancakes", null, null, null) }
            assertThrows<OAuthException> { service.startAuthorization("pancakes", redirectUri, null, "fragment") }
        }
    }

    @Test
    fun `login requires a nonce issued by an authorization request and consumes it`() {
        runBlocking {
            val page = service.startAuthorization("pancakes", redirectUri, null, null)
            val idToken = telegramLogin(page.nonce)
            service.completeLogin(idToken)
            assertThat(assertThrows<OAuthException> { service.completeLogin(idToken) }.error).isEqualTo("access_denied")
            assertThrows<OAuthException> { service.completeLogin(telegramLogin("forged-nonce")) }
            assertThrows<OAuthException> { service.completeLogin(telegramLogin(null)) }
            assertThrows<OAuthException> { service.completeLogin("unverifiable") }
        }
    }

    @Test
    fun `code can be exchanged only once`() {
        val code = authorizeAndLogin()
        runBlocking {
            service.exchangeCode(tokenRequest(code))
            assertThat(assertThrows<OAuthException> { service.exchangeCode(tokenRequest(code)) }.error)
                .isEqualTo("invalid_grant")
        }
    }

    @Test
    fun `code exchange requires the client secret`() {
        val code = authorizeAndLogin()
        runBlocking {
            val error = assertThrows<OAuthException> { service.exchangeCode(tokenRequest(code, secret = "wrong")) }
            assertThat(error.error).isEqualTo("invalid_client")
            assertThat(error.httpStatus).isEqualTo(401)
            assertThat(service.exchangeCode(tokenRequest(code)).user.id).isEqualTo(34080460L)
        }
    }

    @Test
    fun `code issued to one client cannot be redeemed by another`() {
        val otherSecret = "other-secret"
        clients["other"] = pancakeClient.copy(clientId = "other", secretHash = SecureTokens.sha256Hex(otherSecret))
        val code = authorizeAndLogin()
        runBlocking {
            val error = assertThrows<OAuthException> {
                service.exchangeCode(tokenRequest(code, clientId = "other", secret = otherSecret))
            }
            assertThat(error.error).isEqualTo("invalid_grant")
        }
    }

    @Test
    fun `code exchange requires the same redirect uri`() {
        val code = authorizeAndLogin()
        runBlocking {
            assertThrows<OAuthException> {
                service.exchangeCode(tokenRequest(code, redirect = "http://localhost:5173/auth/callback"))
            }
        }
    }

    @Test
    fun `only authorization_code grant is supported`() {
        runBlocking {
            val error = assertThrows<OAuthException> {
                service.exchangeCode(tokenRequest("x", grantType = "client_credentials"))
            }
            assertThat(error.error).isEqualTo("unsupported_grant_type")
        }
    }
}
