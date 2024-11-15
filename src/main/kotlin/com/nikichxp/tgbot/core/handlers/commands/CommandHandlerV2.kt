package com.nikichxp.tgbot.core.handlers.commands

import com.nikichxp.tgbot.core.dto.Update
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
abstract class CommandHandlerV2 : AbstractCommandHandler {

    private val registeredCommands: MutableMap<String, suspend (List<String>, Update) -> Boolean> = mutableMapOf()

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun isCommandSupported(command: String): Boolean = registeredCommands.containsKey(command)
    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        return registeredCommands[command]?.invoke(args, update) ?: false
    }

    protected fun registerCommand(command: String, handler: suspend (List<String>, Update) -> Boolean) {
        if (registeredCommands.containsKey(command)) {
            throw IllegalArgumentException("Command $command is already registered")
        }
        log.info("Registering command $command")
        registeredCommands[command] = handler
    }

    companion object {
        private val registeredHandlers: MutableSet<CommandHandlerV2> = mutableSetOf()
    }
}