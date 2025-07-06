package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.TgBot
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SendMessageToAdminService(
    private val tgOperations: TgOperations,
    private val appConfig: AppConfig
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    var adminId: Long = appConfig.adminId

    suspend fun sendMessage(message: String) {
        if (adminId == 0L) {
            return
        }

        try {
            tgOperations.sendMessage(TgBot.NIKICHBOT) {
                chatId = adminId
                text = message
            }
        } catch (e: Exception) {
            log.warn("Failed to send version update message to bot ${TgBot.NIKICHBOT}", e)
        }

    }

}