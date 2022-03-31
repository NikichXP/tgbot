package com.nikichxp.tgbot.handlers

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.service.TgOperations
import com.nikichxp.tgbot.service.menu.CommandHandler
import com.nikichxp.tgbot.util.getContextChatId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ChatCommandsHandler(
    private val tgOperations: TgOperations,
    private val commandHandlers: List<CommandHandler>
) : UpdateHandler {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.HAS_TEXT)

    override fun handleUpdate(update: Update) {
        val query = update.message!!.text!!.split(" ")
        val result = commandHandlers
            .find { it.isCommandSupported(query.first()) }
            ?.processCommand(query.drop(1))
            ?: false
        if (!result) {
//            val chatId = update.getContextChatId()!!
//            tgOperations.sendMessage(chatId.toString(), "Unknown command")
        } else {
            logger.info("chadId = ${update.getContextChatId()} :: successfully handled command ${update.message.text}")
        }
    }

}