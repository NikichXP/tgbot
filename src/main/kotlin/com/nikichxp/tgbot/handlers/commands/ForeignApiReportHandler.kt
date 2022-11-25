package com.nikichxp.tgbot.handlers.commands

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nikichxp.tgbot.service.TgOperations
import com.nikichxp.tgbot.service.menu.CommandHandler
import com.nikichxp.tgbot.util.ChatCommandParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class ForeignApiReportHandler(
    private val tgOperations: TgOperations,
    private val objectMapper: ObjectMapper
) : CommandHandler {

    suspend fun getStatusOfNode(): String {
        val response = client.get("https://route.nikichxp.xyz/status")
        val body: String = response.body()
        val tree = objectMapper.readTree(body)
        return (tree["metrics"]["events"]["intensity"] as ObjectNode).fields()
            .asSequence()
            .map { "${it.key}: ${it.value.asDouble()}/s" }
            .reduce { acc, s -> "$acc\n$s" }
    }

    private val client = HttpClient(CIO) {
        engine {
            maxConnectionsCount = 1000
            requestTimeout = 60_000
            endpoint {
                maxConnectionsPerRoute = 100
                pipelineMaxSize = 20
                keepAliveTime = 5000
                connectTimeout = 5000
                connectAttempts = 5
            }
        }
    }

    override fun isCommandSupported(command: String): Boolean = command == "/status"

    @OptIn(DelicateCoroutinesApi::class)
    override fun processCommand(args: List<String>): Boolean {
        return ChatCommandParser.analyze(args) {
            asArg("resource") {
                when (vars["resource"]) {
                    "route" -> {
                        tgOperations.replyToCurrentMessage("Started operation")
                        GlobalScope.launch {
                            tgOperations.replyToCurrentMessage(getStatusOfNode())
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