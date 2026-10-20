package com.nikichxp.tgbot.core.tooling

import ch.qos.logback.classic.Logger
import com.nikichxp.bot.common.config.AppConfig
import com.nikichxp.tgbot.core.service.ITgBotV2Service
import com.nikichxp.tgbot.core.service.tgapi.ITgMessageService
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@Service
class TelegramLogChannelService(
    private val tgMessageService: ITgMessageService,
    private val botV2Service: ITgBotV2Service,
    private val appConfig: AppConfig
) {

    private val queue = LinkedBlockingQueue<BotAppLogEvent>(QUEUE_CAPACITY)
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

        runCatching {
            val bot = botV2Service.getAdminBot()
            runBlocking {
                events.joinToString("\n") { it.toFormattedString() }.chunked(MAX_MESSAGE_LENGTH).forEach { part ->
                    tgMessageService.sendMessage(bot) {
                        chatId = channelId
                        text = part
                    }
                }
            }
        }.onFailure { LoggerFactory.getLogger(this::class.java).warn("Failed to send logs to channel {}: {}", channelId, it.message) }
    }

    companion object {
        private const val QUEUE_CAPACITY = 10_000
        private const val MAX_MESSAGE_LENGTH = 4000
    }
}
