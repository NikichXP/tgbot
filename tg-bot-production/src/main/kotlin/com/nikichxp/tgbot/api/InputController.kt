package com.nikichxp.tgbot.api

import com.nikichxp.bot.common.config.AppConfig
import com.nikichxp.bot.common.error.NotAuthorizedException
import com.nikichxp.bot.discord.dto.InteractionResponse
import com.nikichxp.bot.discord.service.IDiscordInteractionService
import com.nikichxp.tgbot.core.service.ITgBotV2Service
import com.nikichxp.tgbot.core.service.MessageEntryPoint
import com.nikichxp.tgbot.core.tooling.TracerService
import org.bson.Document
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class InputController(
    private val tgBotV2Service: ITgBotV2Service,
    private val messageEntryPoint: MessageEntryPoint,
    private val appConfig: AppConfig,
    private val tracerService: TracerService,
    private val discordInteractionService: IDiscordInteractionService
) {

    @Bean
    fun router() = coRouter {
        GET("/echo") {
            ServerResponse.ok().bodyValueAndAwait("ok")
        }

        POST("/handle/{bot}") {
            val botId = it.pathVariable("bot")
            val botV2Entity = tgBotV2Service.getBotById(botId)
            val body = it.awaitBody<Document>()
            messageEntryPoint.proceedRawData(body, botV2Entity)
            // TODO return ok only if one of handlers/all supported handlers processed the message
            ServerResponse.ok().bodyValueAndAwait("ok")
        }

        POST("/discord/{token}") {
            val token = it.pathVariable("token")
            val signature = it.headers().firstHeader("X-Signature-Ed25519")
            val timestamp = it.headers().firstHeader("X-Signature-Timestamp")
            val bodyString = it.awaitBody<String>()

            when (val response = discordInteractionService.handleInteraction(signature, timestamp, bodyString, token)) {
                is InteractionResponse.Unauthenticated -> ServerResponse.status(401).bodyValueAndAwait("Invalid signature")
                is InteractionResponse.Pong -> ServerResponse.ok().bodyValueAndAwait(Document("type", 1))
                is InteractionResponse.Ok -> ServerResponse.ok().bodyValueAndAwait("ok")
            }
        }

        path("/tracer").nest {
            GET("/list") {
                authenticate(it)
                ServerResponse.ok().bodyValueAndAwait(tracerService.list())
            }
        }

        onError<Exception> { err, _ ->
            when (err) {
                is NotAuthorizedException -> status(403).bodyValueAndAwait("Fuck off")
                is IllegalArgumentException -> status(405).bodyValueAndAwait("Illegal arg: ${err.localizedMessage}")
                else -> status(503).bodyValueAndAwait(err)
            }
        }
    }

    private fun authenticate(request: ServerRequest) {
        val submittedToken = request.headers().header("token").firstOrNull()
        if (submittedToken != appConfig.tracer.token) {
            throw NotAuthorizedException()
        }
    }

}
