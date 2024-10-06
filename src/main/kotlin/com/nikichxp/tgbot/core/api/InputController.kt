package com.nikichxp.tgbot.core.api

import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.service.MessageEntryPoint
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class InputController(
    private val messageEntryPoint: MessageEntryPoint
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
        onError<Exception> { err, _ ->
            when (err) {
                is IllegalArgumentException -> status(405).bodyValueAndAwait("Illegal arg: ${err.localizedMessage}")
                else -> status(503).bodyValueAndAwait(err)
            }
        }
    }
}