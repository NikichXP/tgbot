package com.nikichxp.tgbot.core.handlers

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.core.handlers.commands.CommandHandlerExecutor
import com.nikichxp.tgbot.core.handlers.commands.CommandHandlerScanner
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ChatCommandsHandler(
    private val commandHandlerScanner: CommandHandlerScanner,
    private val commandHandlerExecutor: CommandHandlerExecutor,
) : UpdateHandler {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val commandHandlerExecutorMap by lazy {
        commandHandlerScanner.getHandlers().groupBy { it.command }
    }

    override fun botSupported(bot: TgBot) = true

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.HAS_TEXT)

    override suspend fun handleUpdate(update: Update) {
        val query = update.message!!.text!!.split(" ")

        val result = commandHandlerExecutorMap[query.first()]?.let {
            it.filter { handler -> handler.handler.supportedBots(update.bot).contains(update.bot) }
                .map { handler -> commandHandlerExecutor.execute(handler, query, update) }
        }

        val log = when {
            result == null -> "unknown command"
            result.isEmpty() -> "no handlers for command ${query.first()} suitable for bot ${update.bot}"
            result.all { it } -> "successfully handled command"
            result.any { it } -> "partially handled command (${result.count { it }}/${result.size})"
            else -> "failed executing command"
        }

        logger.info("chadId = ${update.getContextChatId()} | ${update.message.text} | $log")
    }

}