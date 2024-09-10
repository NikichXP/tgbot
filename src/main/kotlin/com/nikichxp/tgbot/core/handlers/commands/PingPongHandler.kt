package com.nikichxp.tgbot.core.handlers.commands

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.springframework.stereotype.Component

@Component
class PingPongHandler(
    private val tgOperations: TgOperations
) : CommandHandler {

    override fun supportedBots(tgBot: TgBot) = TgBot.entries.toSet()

    override fun isCommandSupported(command: String): Boolean = command == "/ping"

    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        tgOperations.replyToCurrentMessage("pong!")
        return true
    }
}