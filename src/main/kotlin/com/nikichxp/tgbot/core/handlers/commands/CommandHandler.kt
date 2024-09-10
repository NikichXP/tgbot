package com.nikichxp.tgbot.core.handlers.commands

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateContext
import org.springframework.stereotype.Indexed

@Indexed
interface CommandHandler {
    fun supportedBots(tgBot: TgBot): Set<TgBot>
    fun isCommandForBotSupported(tgBot: TgBot): Boolean = supportedBots(tgBot).contains(tgBot)
    fun isCommandSupported(command: String): Boolean
    suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean
    suspend fun processCommand(args: List<String>, command: String, updateContext: UpdateContext): Boolean =
        processCommand(args, command, updateContext.update)
}