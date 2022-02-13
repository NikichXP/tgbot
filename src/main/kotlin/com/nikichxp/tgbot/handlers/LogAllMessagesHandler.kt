package com.nikichxp.tgbot.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.dto.Message
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.service.TgOperations
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LogAllMessagesHandler(
    private val tgOperations: TgOperations,
    private val objectMapper: ObjectMapper
) : UpdateHandler {

    private val loggingToAllSet = mutableSetOf<Pair<Long, Boolean>>()
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun getMarkers(): Set<UpdateMarker> {
        return setOf(UpdateMarker.ALL)
    }

    override fun handleUpdate(update: Update) {
        val chatId = getContextChatId(update)
        update.message?.text?.also { text ->
            chatId ?: return@also
            when (text) {
                "/logging this on" -> {
                    loggingToAllSet.add(chatId to false)
                }
                "/logging" -> {
                    tgOperations.sendMessage(chatId.toString(), "[INFO] Logging status is: "
                            + (loggingToAllSet.find { it.first == chatId }?.second ?: false)
                    )
                }
                "/logging this off" -> {
                    loggingToAllSet.removeIf { it.first == chatId }
                }
            }
        }

        if (loggingToAllSet.isNotEmpty()) {
            logger.info(objectMapper.writeValueAsString(update))
        }
        loggingToAllSet.find { it.first == chatId }?.let {
            if (!it.second) {
                if (update.message?.text?.startsWith(prefix) == false)
                    tgOperations.sendMessage(chatId.toString(), prefix + objectMapper.writeValueAsString(update))
            }
        }
    }

    private fun getContextChatId(update: Update): Long? {
        fun getChatId(message: Message?) = message?.chat?.id
        return getChatId(update.message)
            ?: getChatId(update.editedMessage)
            ?: getChatId(update.editedChannelPost)
            ?: getChatId(update.channelPost)
            ?: getChatId(update.callbackQuery?.message)
    }

    companion object {
        const val prefix = "[logger]: "
    }

}