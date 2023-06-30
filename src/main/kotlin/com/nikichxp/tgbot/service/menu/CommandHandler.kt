package com.nikichxp.tgbot.service.menu

import com.nikichxp.tgbot.dto.Update
import org.springframework.stereotype.Indexed

@Indexed
interface CommandHandler {
    fun isCommandSupported(command: String): Boolean
    fun processCommand(args: List<String>, update: Update): Boolean
}