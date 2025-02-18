package com.nikichxp.tgbot.core.api

import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.error.NotAuthorizedException
import com.nikichxp.tgbot.core.service.MessageEntryPoint
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.*

@Configuration
class InputController(
    private val messageEntryPoint: MessageEntryPoint,
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val botMap = TgBot.entries.associateBy { it.botName }

    @Bean
    fun router() = coRouter {
        GET("/echo") {
            ServerResponse.ok().bodyValueAndAwait("ok")
        }
        POST("/handle/{bot}") {
            val botToken = it.pathVariable("bot")
            val botEntity = botMap[botToken] ?: run {
                logger.error("Unknown bot handle command: $botToken")
                throw IllegalArgumentException()
            }
            val body = it.awaitBody<Document>()
            messageEntryPoint.proceedRawData(body, botEntity)
            // TODO return ok only if one of handlers/all supported handlers processed the message
            ServerResponse.ok().bodyValueAndAwait("ok")
        }
        path("/tracer").nest {
            GET("/") {
                val token = getToken(it)
                ServerResponse.ok().bodyValueAndAwait("test response: $token")
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

    private fun getToken(request: ServerRequest) =
        request.headers().header("token").firstOrNull() ?: throw NotAuthorizedException()

}
