package com.nikichxp.tgbot.okx

import com.nikichxp.tgbot.core.service.TgBotV2Service
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class OkxWatchNotificationListener(
    private val tgMessageService: TgMessageService,
    private val tgBotV2Service: TgBotV2Service,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @RabbitListener(queues = [OKX_WATCH_NOTIFICATIONS_QUEUE])
    fun onNotification(notification: WatchNotification) {
        val bot = try {
            tgBotV2Service.getBotById(notification.botName)
        } catch (e: IllegalArgumentException) {
            logger.warn("Dropping watch notification for unknown bot: {}", notification)
            return
        }
        val text = WatchNotificationFormatter.format(notification)
        runBlocking {
            tgMessageService.sendMessage(bot) {
                chatId = notification.chatId
                this.text = text
            }
        }
    }
}
