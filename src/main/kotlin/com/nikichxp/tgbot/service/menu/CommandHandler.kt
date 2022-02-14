package com.nikichxp.tgbot.service.menu

import org.springframework.stereotype.Indexed

@Indexed
interface CommandHandler {
    fun isCommandSupported(command: String): Boolean
    fun processCommand(args: List<String>): Boolean
}