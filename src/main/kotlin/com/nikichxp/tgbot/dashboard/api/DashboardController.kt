package com.nikichxp.tgbot.dashboard.api

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.dashboard.dto.AuthConfigResponse
import com.nikichxp.tgbot.dashboard.dto.CreateBotRequest
import com.nikichxp.tgbot.dashboard.dto.CreateOAuthClientRequest
import com.nikichxp.tgbot.dashboard.dto.LoginResponse
import com.nikichxp.tgbot.dashboard.dto.NonceResponse
import com.nikichxp.tgbot.dashboard.dto.RefreshResponse
import com.nikichxp.tgbot.dashboard.dto.TelegramLoginRequest
import com.nikichxp.tgbot.dashboard.dto.UpdateBotFeaturesRequest
import com.nikichxp.tgbot.dashboard.error.DashboardUnauthorizedException
import com.nikichxp.tgbot.dashboard.service.DashboardAuthService
import com.nikichxp.tgbot.dashboard.service.DashboardBotService
import com.nikichxp.tgbot.dashboard.service.DashboardOAuthClientService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter
import java.time.Duration

@Configuration
class DashboardController(
    private val appConfig: AppConfig,
    private val authService: DashboardAuthService,
    private val botService: DashboardBotService,
    private val oauthClientService: DashboardOAuthClientService,
    private val apiSupport: DashboardApiSupport
) {

    @Bean
    fun dashboardRouter() = coRouter {
        "/admin/auth".nest {
            GET("/config") {
                ok().bodyValueAndAwait(AuthConfigResponse(authService.telegramClientId()))
            }

            POST("/nonce") {
                ok().bodyValueAndAwait(NonceResponse(authService.issueLoginNonce()))
            }

            POST("/telegram") { request ->
                val body = request.awaitBody<TelegramLoginRequest>()
                val userAgent = request.headers().firstHeader(HttpHeaders.USER_AGENT)
                val result = authService.loginWithTelegram(body.idToken, userAgent)
                ok().cookie(refreshCookie(result.tokens.refreshToken, REFRESH_COOKIE_MAX_AGE))
                    .bodyValueAndAwait(
                        LoginResponse(result.tokens.accessToken, result.tokens.expiresIn.seconds, result.user)
                    )
            }

            POST("/refresh") { request ->
                val refreshToken = request.refreshTokenCookie()
                    ?: throw DashboardUnauthorizedException("No refresh token")
                val (accessToken, ttl) = authService.refresh(refreshToken)
                ok().bodyValueAndAwait(RefreshResponse(accessToken, ttl.seconds))
            }

            POST("/logout") { request ->
                authService.logout(request.refreshTokenCookie(), request.bearerToken())
                noContent().cookie(refreshCookie("", Duration.ZERO)).buildAndAwait()
            }
        }

        "/admin".nest {
            filter { request, next ->
                apiSupport.authenticate(request)
                next(request)
            }

            GET("/me") { request ->
                ok().bodyValueAndAwait(authService.currentUser(apiSupport.principal(request)))
            }

            GET("/features") {
                ok().bodyValueAndAwait(botService.availableFeatures())
            }

            GET("/bots") {
                ok().bodyValueAndAwait(botService.listBots())
            }

            POST("/bots") { request ->
                ok().bodyValueAndAwait(botService.createBot(request.awaitBody<CreateBotRequest>()))
            }

            PUT("/bots/{name}/features") { request ->
                val body = request.awaitBody<UpdateBotFeaturesRequest>()
                ok().bodyValueAndAwait(botService.updateFeatures(request.pathVariable("name"), body))
            }

            GET("/oauth-clients") {
                ok().bodyValueAndAwait(oauthClientService.listClients())
            }

            POST("/oauth-clients") { request ->
                ok().bodyValueAndAwait(oauthClientService.createClient(request.awaitBody<CreateOAuthClientRequest>()))
            }

            POST("/oauth-clients/{clientId}/secret") { request ->
                ok().bodyValueAndAwait(oauthClientService.rotateSecret(request.pathVariable("clientId")))
            }

            DELETE("/oauth-clients/{clientId}") { request ->
                oauthClientService.deleteClient(request.pathVariable("clientId"))
                noContent().buildAndAwait()
            }
        }

        onError<Exception> { error, _ -> apiSupport.errorResponse(error) }
    }

    private fun refreshCookie(value: String, maxAge: Duration) = ResponseCookie.from(REFRESH_COOKIE, value)
        .httpOnly(true)
        .secure(appConfig.dashboard.secureCookie)
        .sameSite("Strict")
        .path("/admin/auth")
        .maxAge(maxAge)
        .build()

    private fun ServerRequest.refreshTokenCookie(): String? =
        cookies().getFirst(REFRESH_COOKIE)?.value?.takeIf { it.isNotBlank() }

    companion object {
        private const val REFRESH_COOKIE = "tgbot_dashboard_refresh"

        private val REFRESH_COOKIE_MAX_AGE = Duration.ofDays(3650)
    }
}
