package com.nikichxp.tgbot.api

import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.service.MessageEntryPoint
import org.bson.Document
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class InputController(
    private val messageEntryPoint: MessageEntryPoint
) {

    private val botMap = TgBot.values().associateBy { it.botName }

    @Bean
    fun router() = coRouter {
        GET("/echo") {
            ServerResponse.ok().bodyValueAndAwait("ok")
        }
        POST("/handle/{bot}") {
            val bot = it.pathVariable("bot")
            val botEntity = botMap[bot] ?: throw IllegalArgumentException()
            val body = it.awaitBody<Document>()
            messageEntryPoint.proceedRawData(body, botEntity)
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