package com.nikichxp.tgbot.handlers

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.service.TgOperations
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class ChatCommands(
    @Lazy private val logAllMessagesHandler: LogAllMessagesHandler,
    private val tgOperations: TgOperations
) : UpdateHandler {
    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.HAS_TEXT)

    override fun handleUpdate(update: Update) {
        val query = update.message!!.text!!.split(" ")
        val result = when {
            query.first() == "/logging" -> {
                logAllMessagesHandler.configureLogging(query.drop(1))
            }
            else -> false
        }
        if (!result) {
            val chatId = update.getContextChatId()!!
            tgOperations.sendMessage(chatId.toString(), "Unknown command")
        }
    }

}