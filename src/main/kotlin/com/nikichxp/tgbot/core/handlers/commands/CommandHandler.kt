package com.nikichxp.tgbot.core.handlers.commands

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import org.springframework.stereotype.Indexed

@Indexed
interface CommandHandler : AbstractCommandHandler

interface AbstractCommandHandler {
    fun supportedBots(tgBot: TgBot): Set<TgBot>
    fun isCommandForBotSupported(tgBot: TgBot): Boolean = supportedBots(tgBot).contains(tgBot)
    fun isCommandSupported(command: String): Boolean
    suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean
}