package com.nikichxp.tgbot.handlers.commands

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import org.springframework.stereotype.Indexed

@Indexed
interface CommandHandler {
    fun supportedBots(tgBot: TgBot): Set<TgBot>
    fun isCommandForBotSupported(tgBot: TgBot): Boolean = supportedBots(tgBot).contains(tgBot)
    fun isCommandSupported(command: String): Boolean
    fun processCommand(args: List<String>, command: String, update: Update): Boolean
}