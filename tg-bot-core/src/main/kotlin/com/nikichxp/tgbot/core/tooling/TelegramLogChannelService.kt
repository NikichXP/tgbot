package com.nikichxp.tgbot.core.tooling

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.nikichxp.bot.common.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.service.ITgBotV2Service
import com.nikichxp.tgbot.core.service.tgapi.ITgMessageService
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@Service
class TelegramLogChannelService(
    private val tgMessageService: ITgMessageService,
    private val botV2Service: ITgBotV2Service,
    private val appConfig: AppConfig,
    private val coroutineScope: CoroutineScope
) {

    private val queue = LinkedBlockingQueue<BotAppLogEvent>(QUEUE_CAPACITY)
    private val logger = LoggerFactory.getLogger(javaClass)
    private val appender = TelegramLogAppender(queue).apply { name = "TELEGRAM_CHANNEL" }
    private val rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger

    @PostConstruct
    fun attach() {
        if (appConfig.logChannelId == null) return
        appender.context = rootLogger.loggerContext
        appender.start()
        rootLogger.addAppender(appender)
    }

    @PreDestroy
    fun detach() {
        rootLogger.detachAppender(appender)
        flush()
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    fun flush() {
        val channelId = appConfig.logChannelId ?: return
        val events = mutableListOf<BotAppLogEvent>()
        queue.drainTo(events)
        if (events.isEmpty()) return

        coroutineScope.launch {
            val bot = botV2Service.getAdminBot()
            events.forEach { routeAndSend(bot, channelId, it) }
        }
    }

    private suspend fun routeAndSend(bot: TgBotInfo, logChannelId: Long, event: BotAppLogEvent) {
        try {
            if (event.level.isGreaterOrEqual(Level.INFO)) send(bot, logChannelId, event)
            appConfig.debugLogChannelId?.let { send(bot, it, event) }
        } catch (e: Exception) {
            logger.warn("Failed to send logs to channel {}: {}...", logChannelId, e.message?.take(3000))
        }
    }

    private suspend fun send(bot: TgBotInfo, channelId: Long, event: BotAppLogEvent) {
        tgMessageService.sendMessage(bot) {
            chatId = channelId
            text = event.toFormattedString()
        }
    }

    companion object {
        private const val QUEUE_CAPACITY = 10_000
        private const val MAX_MESSAGE_LENGTH = 4000
    }
}
