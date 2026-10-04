package com.nikichxp.tgbot.core.service

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.service.tgapi.ITgMessageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AdminNotificationServiceImpl(
    private val tgMessageService: ITgMessageService,
    private val botV2Service: ITgBotV2Service,
    private val appConfig: AppConfig
) : IAdminNotificationService {

    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun notifyAdmin(message: String) {
        val adminId: Long = appConfig.adminId

        if (adminId == 0L) {
            return
        }

        try {
            tgMessageService.sendMessage(botV2Service.getAdminBot()) {
                chatId = adminId
                text = message
            }
        } catch (e: Exception) {
            log.warn("Failed to send notification to admin", e)
        }

    }

}
