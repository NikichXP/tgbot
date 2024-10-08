package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SendMessageToAdminService(
    private val tgOperations: TgOperations
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Value("\${app.admin-id}")
    var adminId: Long = 0

    suspend fun sendMessage(message: String) {
        if (adminId == 0L) {
            return
        }

        try {
            tgOperations.sendMessage(
                chatId = adminId,
                text = message,
                tgBot = TgBot.NIKICHBOT
            )
        } catch (e: Exception) {
            log.warn("Failed to send version update message to bot ${TgBot.NIKICHBOT}", e)
        }

    }

}