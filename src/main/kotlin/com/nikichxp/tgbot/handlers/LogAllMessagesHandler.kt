package com.nikichxp.tgbot.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.dto.Message
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.service.TgOperations
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class LogAllMessagesHandler(
    private val tgOperations: TgOperations,
    private val objectMapper: ObjectMapper
) : UpdateHandler {

    @Value("\${ADMIN_USER:0}")
    private var adminUser: Long = 0

    private val loggingToModeMap = mutableMapOf<Long, Boolean>()
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
                    loggingToModeMap[chatId] = false
                }
                "/logging" -> {
                    tgOperations.sendMessage(chatId.toString(), "[INFO] Logging status is: "
                            + (loggingToModeMap[chatId] ?: false)
                    )
                }
                "/logging this off" -> {
                    loggingToModeMap.remove(chatId)
                }
                "/logging all on" -> {
                    if (chatId == adminUser) {
                        loggingToModeMap[chatId] = true
                    }
                }
            }
        }

        if (loggingToModeMap.isNotEmpty()) {
            logger.info(objectMapper.writeValueAsString(update))
        }
        loggingToModeMap[chatId]?.let {
            if (!it) {
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