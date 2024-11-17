package com.nikichxp.tgbot.core.handlers

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandlerV2
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.debug.CommandHandlerExecutor
import com.nikichxp.tgbot.debug.CommandHandlerScanner
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ChatCommandsHandler(
    private val commandHandlers: List<CommandHandler>,
    private val commandHandlersV2: List<CommandHandlerV2>,
    private val commandHandlerScanner: CommandHandlerScanner,
    private val commandHandlerExecutor: CommandHandlerExecutor,
) : UpdateHandler {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val commandHandlerExecutorMap by lazy {
        commandHandlerScanner.getHandlers().associateBy { it.command }
    }

    override fun botSupported(bot: TgBot) = true

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.HAS_TEXT)

    override suspend fun handleUpdate(update: Update) {
        val query = update.message!!.text!!.split(" ")
        val result = commandHandlers
            .find { it.isCommandSupported(query.first()) && it.isCommandForBotSupported(update.bot) }
            ?.processCommand(query.drop(1), query.first(), update)

        val resultV2 = commandHandlerExecutorMap[query.first()]?.let {
            commandHandlerExecutor.execute(it, query, update)
        }

        val log = when (result ?: resultV2) {
            true -> "successfully handled command"
            false -> "failed executing command"
            null -> "unknown command"
        }

        logger.info("chadId = ${update.getContextChatId()} | ${update.message.text} | $log")
    }

}