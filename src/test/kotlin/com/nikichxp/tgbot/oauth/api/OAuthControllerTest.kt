package com.nikichxp.tgbot.oauth.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.dashboard.service.TelegramIdTokenVerifier
import com.nikichxp.tgbot.oauth.dto.OAuthAuthorizePage
import com.nikichxp.tgbot.oauth.dto.OAuthResponseMode
import com.nikichxp.tgbot.oauth.dto.OAuthTokenRequest
import com.nikichxp.tgbot.oauth.dto.OAuthTokenResponse
import com.nikichxp.tgbot.oauth.dto.OAuthUserDto
import com.nikichxp.tgbot.oauth.error.OAuthException
import com.nikichxp.tgbot.oauth.repository.OAuthAuthorizationCodeRepository
import com.nikichxp.tgbot.oauth.repository.OAuthClientRepository
import com.nikichxp.tgbot.oauth.repository.OAuthLoginRequestRepository
import com.nikichxp.tgbot.oauth.service.OAuthAuthorizationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.util.Base64

class OAuthControllerTest {

    private var lastTokenRequest: OAuthTokenRequest? = null

    private val service = object : OAuthAuthorizationService(
        mock(AppConfig::class.java),
        mock(TelegramIdTokenVerifier::class.java),
        mock(OAuthClientRepository::class.java),
        mock(OAuthLoginRequestRepository::class.java),
        mock(OAuthAuthorizationCodeRepository::class.java)
    ) {
        override suspend fun startAuthorization(
            clientId: String?,
            redirectUri: String?,
            state: String?,
            responseMode: String?
        ): OAuthAuthorizePage {
            if (clientId != "pancakes") throw OAuthException.invalidRequest("Unknown client_id")
            return OAuthAuthorizePage("1", "n", "Pancake Club", OAuthResponseMode.WEB_MESSAGE, listOf("https://pancakes.example"))
        }

        override suspend fun exchangeCode(request: OAuthTokenRequest): OAuthTokenResponse {
            lastTokenRequest = request
            if (request.clientSecret != "s3cret") throw OAuthException.invalidClient()
            return OAuthTokenResponse(OAuthUserDto(42, "Nik", "nik", null), 1000)
        }
    }

    private val client = WebTestClient.bindToRouterFunction(
        OAuthController(service, OAuthAuthorizePageRenderer(jacksonObjectMapper())).oauthRouter()
    ).build()

    private fun tokenForm() = BodyInserters.fromFormData("grant_type", "authorization_code")
        .with("code", "c-1")
        .with("redirect_uri", "https://pancakes.example/cb")

    @Test
    fun `authorize renders the login page framable only by the client`() {
        client.get().uri("/oauth/authorize?client_id=pancakes&redirect_uri=https://pancakes.example/cb&response_mode=web_message")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
            .expectHeader().valueEquals("Content-Security-Policy", "frame-ancestors https://pancakes.example")
            .expectHeader().valueEquals(HttpHeaders.CACHE_CONTROL, "no-store")
    }

    @Test
    fun `authorize with an unknown client shows an error instead of redirecting`() {
        client.get().uri("/oauth/authorize?client_id=evil&redirect_uri=https://evil.example/cb")
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().doesNotExist(HttpHeaders.LOCATION)
            .expectBody(String::class.java).isEqualTo("invalid_request: Unknown client_id")
    }

    @Test
    fun `token accepts client credentials in the form body`() {
        client.post().uri("/oauth/token")
            .body(tokenForm().with("client_id", "pancakes").with("client_secret", "s3cret"))
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.user.id").isEqualTo(42).jsonPath("$.authTime").isEqualTo(1000)
        assertThat(lastTokenRequest).isEqualTo(
            OAuthTokenRequest("authorization_code", "c-1", "https://pancakes.example/cb", "pancakes", "s3cret")
        )
    }

    @Test
    fun `token accepts client credentials via basic auth`() {
        val basic = Base64.getEncoder().encodeToString("pancakes:s3cret".toByteArray())
        client.post().uri("/oauth/token")
            .header(HttpHeaders.AUTHORIZATION, "Basic $basic")
            .body(tokenForm())
            .exchange()
            .expectStatus().isOk
        assertThat(lastTokenRequest?.clientId).isEqualTo("pancakes")
        assertThat(lastTokenRequest?.clientSecret).isEqualTo("s3cret")
    }

    @Test
    fun `token errors use the oauth error format`() {
        client.post().uri("/oauth/token")
            .body(tokenForm().with("client_id", "pancakes").with("client_secret", "wrong"))
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .jsonPath("$.error").isEqualTo("invalid_client")
            .jsonPath("$.error_description").isEqualTo("Client authentication failed")
    }

    @Test
    fun `embed script is served as javascript`() {
        client.get().uri("/oauth/embed.js")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType("text", "javascript"))
            .expectBody(String::class.java).value { assertThat(it).contains("TgBotAuth") }
    }
}
