package com.nikichxp.tgbot.warehousebot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.ChatCommandParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class ForeignApiReportHandler(
    private val tgOperations: TgOperations,
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) : CommandHandler {

    override fun supportedBots(tgBot: TgBot) = setOf(TgBot.NIKICHBOT)

    suspend fun getStatusOfNode(): String {
        val response = client.get("https://route.nikichxp.xyz/status")
        val body: String = response.body()
        val tree = objectMapper.readTree(body)
        return (tree["metrics"]["events"]["intensity"] as ObjectNode).fields()
            .asSequence()
            .map { "${it.key}: ${it.value.asDouble()}/s" }
            .reduce { acc, s -> "$acc\n$s" }
    }

    override fun isCommandSupported(command: String): Boolean = command == "/status"

    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        return ChatCommandParser.analyze(args) {
            asArg("resource") {
                when (vars["resource"]) {
                    "route" -> {
                        coroutineScope {
                            launch {
                                tgOperations.replyToCurrentMessage("Started operation")
                                tgOperations.replyToCurrentMessage(getStatusOfNode())
                            }
                        }
                    }
                    else -> {
                        tgOperations.replyToCurrentMessage("no such service")
                    }
                }
            }
        }
    }
}