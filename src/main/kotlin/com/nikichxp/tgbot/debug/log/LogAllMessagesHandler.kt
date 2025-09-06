package com.nikichxp.tgbot.debug.log

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.getContextChatId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LogAllMessagesHandler(
    private val tgOperations: TgOperations,
    private val objectMapper: ObjectMapper,
    private val loggingConfigBackend: LoggingConfigBackend
) : UpdateHandler {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun requiredFeatures() = setOf<String>()

    override fun getMarkers(): Set<UpdateMarker> = UpdateMarker.entries.toSet()

    override suspend fun handleUpdate(update: Update) {
        val chatId = update.getContextChatId()

        if (update.message?.text?.startsWith(LOG_PREFIX) == true) {
            return
        }

        if (chatId != null && loggingConfigBackend.shouldLog(chatId)) {
            logger.info(objectMapper.writeValueAsString(update))

            if (!loggingConfigBackend.isAdmin(chatId)) {
                tgOperations.sendMessage(chatId, LOG_PREFIX + objectMapper.writeValueAsString(update))
            }
        }

        if (chatId != null && loggingConfigBackend.isAdmin(chatId)) {
            tgOperations.sendMessage(chatId, LOG_PREFIX + objectMapper.writeValueAsString(update))
        }
    }

    companion object {
        const val LOG_PREFIX = "[logger]: "
    }
}

