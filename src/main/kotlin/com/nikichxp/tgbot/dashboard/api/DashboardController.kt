package com.nikichxp.tgbot.dashboard.api

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.dashboard.dto.AuthConfigResponse
import com.nikichxp.tgbot.dashboard.dto.CreateBotRequest
import com.nikichxp.tgbot.dashboard.dto.CreateOAuthClientRequest
import com.nikichxp.tgbot.dashboard.dto.DashboardPrincipal
import com.nikichxp.tgbot.dashboard.dto.ErrorResponse
import com.nikichxp.tgbot.dashboard.dto.LoginResponse
import com.nikichxp.tgbot.dashboard.dto.NonceResponse
import com.nikichxp.tgbot.dashboard.dto.RefreshResponse
import com.nikichxp.tgbot.dashboard.dto.TelegramLoginRequest
import com.nikichxp.tgbot.dashboard.dto.UpdateBotFeaturesRequest
import com.nikichxp.tgbot.dashboard.error.DashboardConflictException
import com.nikichxp.tgbot.dashboard.error.DashboardForbiddenException
import com.nikichxp.tgbot.dashboard.error.DashboardNotFoundException
import com.nikichxp.tgbot.dashboard.error.DashboardUnauthorizedException
import com.nikichxp.tgbot.dashboard.service.DashboardAuthService
import com.nikichxp.tgbot.dashboard.service.DashboardBotService
import com.nikichxp.tgbot.dashboard.service.DashboardOAuthClientService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.server.ResponseStatusException
import java.time.Duration

@Configuration
class DashboardController(
    private val appConfig: AppConfig,
    private val authService: DashboardAuthService,
    private val botService: DashboardBotService,
    private val oauthClientService: DashboardOAuthClientService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

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
                val token = request.bearerToken() ?: throw DashboardUnauthorizedException()
                request.attributes()[PRINCIPAL_ATTRIBUTE] = authService.authenticate(token)
                next(request)
            }

            GET("/me") { request ->
                val principal = request.attributes()[PRINCIPAL_ATTRIBUTE] as DashboardPrincipal
                ok().bodyValueAndAwait(authService.currentUser(principal))
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

        onError<Exception> { err, _ ->
            val (status, message) = when (err) {
                is DashboardUnauthorizedException -> 401 to err.message
                is DashboardForbiddenException -> 403 to err.message
                is DashboardNotFoundException -> 404 to err.message
                is DashboardConflictException -> 409 to err.message
                is IllegalArgumentException -> 400 to err.message
                is ResponseStatusException -> err.statusCode.value() to err.reason
                else -> {
                    logger.error("Dashboard request failed", err)
                    500 to "Internal error"
                }
            }
            status(status).bodyValueAndAwait(ErrorResponse(message ?: "Error"))
        }
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

    private fun ServerRequest.bearerToken(): String? =
        headers().firstHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substring("Bearer ".length)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    companion object {
        private const val REFRESH_COOKIE = "tgbot_dashboard_refresh"
        private const val PRINCIPAL_ATTRIBUTE = "dashboardPrincipal"

        private val REFRESH_COOKIE_MAX_AGE = Duration.ofDays(3650)
    }
}
