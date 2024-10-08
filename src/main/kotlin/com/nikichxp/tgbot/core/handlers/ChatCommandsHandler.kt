package com.nikichxp.tgbot.core.handlers

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.getContextChatId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ChatCommandsHandler(
    private val tgOperations: TgOperations,
    private val commandHandlers: List<CommandHandler>
) : UpdateHandler {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun botSupported(bot: TgBot) = true

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.HAS_TEXT)

    override suspend fun handleUpdate(update: Update) {
        val query = update.message!!.text!!.split(" ")
        val result = commandHandlers
            .find { it.isCommandSupported(query.first()) && it.isCommandForBotSupported(update.bot) }
            ?.processCommand(query.drop(1), query.first(), update)
        val log = when (result) {
            true -> "successfully handled command"
            false -> "failed executing command"
            null -> "unknown command"
        }
        logger.info("chadId = ${update.getContextChatId()} | ${update.message.text} | $log")
    }

}