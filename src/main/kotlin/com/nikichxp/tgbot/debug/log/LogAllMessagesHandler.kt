package com.nikichxp.tgbot.debug.log

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LogAllMessagesHandler(
    private val tgMessageService: TgMessageService,
    private val objectMapper: ObjectMapper,
    private val loggingConfigBackend: LoggingConfigBackend
) : UpdateHandler {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun requiredFeatures() = setOf<String>()

    override fun getMarkers(): Set<UpdateMarker> = UpdateMarker.entries.toSet()

    override suspend fun handleUpdate(updateContext: UpdateContext) {
        val chatId = updateContext.chat?.id

        if (updateContext.message?.text?.startsWith(LOG_PREFIX) == true) {
            return
        }

        if (chatId != null && loggingConfigBackend.shouldLog(chatId)) {
            logger.info(objectMapper.writeValueAsString(updateContext))

            if (!loggingConfigBackend.isAdmin(chatId)) {
                tgMessageService.sendMessage(chatId, LOG_PREFIX + objectMapper.writeValueAsString(updateContext))
            }
        }

        if (chatId != null && loggingConfigBackend.isAdmin(chatId)) {
            tgMessageService.sendMessage(chatId, LOG_PREFIX + objectMapper.writeValueAsString(updateContext))
        }
    }

    companion object {
        const val LOG_PREFIX = "[logger]: "
    }
}

