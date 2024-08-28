package com.nikichxp.tgbot.handlers.commands

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.entity.UpdateContextHandler
import com.nikichxp.tgbot.service.tgapi.TgOperations
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class PingPongHandler(
    private val tgOperations: TgOperations
) : CommandHandler {

    override fun supportedBots(tgBot: TgBot) = TgBot.values().asList().toSet()

    override fun isCommandSupported(command: String): Boolean = command == "/ping"

    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        val context = coroutineScope {
            return@coroutineScope this.coroutineContext[UpdateContextHandler]
        }
        if (context == null) {
            tgOperations.replyToCurrentMessage("pong - no coroutines", update)
        } else {
            tgOperations.replyToCurrentMessage("pong - from context!", update)
        }
        return true
    }
}