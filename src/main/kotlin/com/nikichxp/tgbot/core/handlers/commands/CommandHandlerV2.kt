package com.nikichxp.tgbot.core.handlers.commands

import com.nikichxp.tgbot.core.dto.Update
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
abstract class CommandHandlerV2 : AbstractCommandHandler {

    private val registeredCommands: MutableMap<String, suspend (List<String>, Update) -> Boolean> = mutableMapOf()

    override fun isCommandSupported(command: String): Boolean = registeredCommands.containsKey(command)
    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        return registeredCommands[command]?.invoke(args, update) ?: false
    }

}