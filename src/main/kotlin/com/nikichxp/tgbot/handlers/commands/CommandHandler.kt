package com.nikichxp.tgbot.handlers.commands

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import org.springframework.stereotype.Indexed

@Indexed
interface CommandHandler {
    fun supportedBots(tgBot: TgBot): Set<TgBot>
    fun isCommandSupported(command: String): Boolean
    fun processCommand(args: List<String>, command: String, update: Update): Boolean
}