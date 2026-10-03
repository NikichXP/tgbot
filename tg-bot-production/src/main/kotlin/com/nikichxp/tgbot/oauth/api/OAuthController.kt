package com.nikichxp.tgbot.oauth.api

import com.nikichxp.tgbot.oauth.dto.OAuthErrorResponse
import com.nikichxp.tgbot.oauth.dto.OAuthPageLoginRequest
import com.nikichxp.tgbot.oauth.dto.OAuthTokenRequest
import com.nikichxp.tgbot.oauth.error.OAuthException
import com.nikichxp.tgbot.oauth.service.OAuthAuthorizationService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.awaitFormData
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter
import java.net.URLDecoder
import java.time.Duration
import java.util.Base64

@Configuration
class OAuthController(
    private val authorizationService: OAuthAuthorizationService,
    private val pageRenderer: OAuthAuthorizePageRenderer
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val embedScript: String by lazy {
        ClassPathResource(EMBED_SCRIPT_PATH).inputStream.use { String(it.readAllBytes()) }
    }

    @Bean
    fun oauthRouter() = coRouter {
        "/oauth".nest {
            GET("/authorize") { request ->
                val page = try {
                    authorizationService.startAuthorization(
                        clientId = request.queryParamOrNull("client_id"),
                        redirectUri = request.queryParamOrNull("redirect_uri"),
                        state = request.queryParamOrNull("state"),
                        responseMode = request.queryParamOrNull("response_mode")
                    )
                } catch (e: OAuthException) {
                    return@GET status(e.httpStatus)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValueAndAwait("${e.error}: ${e.description}")
                }
                ok().contentType(MediaType.TEXT_HTML)
                    .cacheControl(CacheControl.noStore())
                    .header(CONTENT_SECURITY_POLICY, pageRenderer.contentSecurityPolicy(page))
                    .header(X_CONTENT_TYPE_OPTIONS, "nosniff")
                    .bodyValueAndAwait(pageRenderer.render(page))
            }

            POST("/login") { request ->
                val body = request.awaitBody<OAuthPageLoginRequest>()
                ok().cacheControl(CacheControl.noStore())
                    .bodyValueAndAwait(authorizationService.completeLogin(body.idToken))
            }

            POST("/token") { request ->
                ok().cacheControl(CacheControl.noStore())
                    .bodyValueAndAwait(authorizationService.exchangeCode(request.toTokenRequest()))
            }

            GET("/embed.js") {
                ok().contentType(JAVASCRIPT)
                    .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                    .bodyValueAndAwait(embedScript)
            }
        }

        onError<Exception> { err, _ ->
            val (status, body) = when (err) {
                is OAuthException -> err.httpStatus to OAuthErrorResponse(err.error, err.description)
                is IllegalArgumentException -> 400 to OAuthErrorResponse("invalid_request", err.message ?: "Bad request")
                else -> {
                    logger.error("OAuth request failed", err)
                    500 to OAuthErrorResponse("server_error", "Internal error")
                }
            }
            status(status).cacheControl(CacheControl.noStore()).bodyValueAndAwait(body)
        }
    }

    private suspend fun ServerRequest.toTokenRequest(): OAuthTokenRequest {
        val form = awaitFormData()
        val basicCredentials = basicClientCredentials()
        return OAuthTokenRequest(
            grantType = form.getFirst("grant_type"),
            code = form.getFirst("code"),
            redirectUri = form.getFirst("redirect_uri"),
            clientId = basicCredentials?.first ?: form.getFirst("client_id"),
            clientSecret = basicCredentials?.second ?: form.getFirst("client_secret")
        )
    }

    private fun ServerRequest.basicClientCredentials(): Pair<String, String>? {
        val header = headers().firstHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith("Basic ", ignoreCase = true) }
            ?: return null
        val decoded = try {
            String(Base64.getDecoder().decode(header.substring("Basic ".length).trim()))
        } catch (e: IllegalArgumentException) {
            throw OAuthException.invalidClient()
        }
        val separator = decoded.indexOf(':').takeIf { it >= 0 } ?: throw OAuthException.invalidClient()
        return formUrlDecode(decoded.substring(0, separator)) to formUrlDecode(decoded.substring(separator + 1))
    }

    private fun formUrlDecode(value: String): String = URLDecoder.decode(value, Charsets.UTF_8)

    private fun ServerRequest.queryParamOrNull(name: String): String? =
        queryParam(name).orElse(null)?.takeIf { it.isNotEmpty() }

    companion object {
        private const val EMBED_SCRIPT_PATH = "oauth/embed.js"
        private const val CONTENT_SECURITY_POLICY = "Content-Security-Policy"
        private const val X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options"
        private val JAVASCRIPT = MediaType("text", "javascript", Charsets.UTF_8)
    }
}
