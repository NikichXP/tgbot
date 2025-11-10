package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.service.TgBotV2Service
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SendMessageToAdminService(
    private val tgOperations: TgOperations,
    private val botV2Service: TgBotV2Service,
    private val appConfig: AppConfig
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun sendMessage(message: String) {
        val adminId: Long = appConfig.adminId

        if (adminId == 0L) {
            return
        }

        try {
            tgOperations.sendMessage(botV2Service.getAdminBot()) {
                chatId = adminId
                text = message
            }
        } catch (e: Exception) {
            log.warn("Failed to send version update message to admin", e)
        }

    }

}