package com.nikichxp.tgbot.core.tooling

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.nikichxp.bot.common.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.error.TgTooManyRequestsException
import com.nikichxp.tgbot.core.service.ITgBotV2Service
import com.nikichxp.tgbot.core.service.tgapi.ITgMessageService
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

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
    private val sendingLock = Mutex()

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

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    fun flush() {
        if (sendingLock.isLocked) return

        val events = mutableListOf<BotAppLogEvent>()
        queue.drainTo(events)
        if (events.isEmpty()) return

        coroutineScope.launch {
            sendingLock.withLock {
                sendEvents(events)
            }
        }
    }

    private suspend fun sendEvents(events: List<BotAppLogEvent>) {
        val channelId = appConfig.logChannelId ?: return
        val bot = botV2Service.getAdminBot()
        for ((targetChannelId, lines) in linesByChannel(events, channelId)) {
            for (text in packIntoMessages(lines)) {
                sendWithRetryOnRateLimit(bot, targetChannelId, text)
            }
        }
    }

    private fun linesByChannel(events: List<BotAppLogEvent>, logChannelId: Long): Map<Long, List<String>> {
        val linesByChannel = linkedMapOf<Long, MutableList<String>>()
        for (event in events) {
            val line = event.toFormattedString()
            for (targetChannelId in channelsFor(event, logChannelId)) {
                linesByChannel.getOrPut(targetChannelId) { mutableListOf() } += line
            }
        }
        return linesByChannel
    }

    private fun channelsFor(event: BotAppLogEvent, logChannelId: Long) = listOfNotNull(
        logChannelId.takeIf { event.level.isGreaterOrEqual(Level.INFO) },
        appConfig.debugLogChannelId
    )

    private fun packIntoMessages(lines: List<String>): List<String> {
        val messages = mutableListOf<String>()
        val current = StringBuilder()
        for (line in lines.map(::fitIntoMessage)) {
            if (current.isNotEmpty() && current.length + 1 + line.length > MAX_MESSAGE_LENGTH) {
                messages += current.toString()
                current.clear()
            }
            if (current.isNotEmpty()) current.append('\n')
            current.append(line)
        }
        if (current.isNotEmpty()) messages += current.toString()
        return messages
    }

    private fun fitIntoMessage(line: String): String {
        if (line.length <= MAX_MESSAGE_LENGTH) return line
        val truncated = line.take(MAX_MESSAGE_LENGTH)
        return if (truncated.last().isHighSurrogate()) truncated.dropLast(1) else truncated
    }

    private suspend fun sendWithRetryOnRateLimit(bot: TgBotInfo, channelId: Long, text: String) {
        while (true) {
            try {
                send(bot, channelId, text)
                return
            } catch (e: TgTooManyRequestsException) {
                delay(maxOf(MIN_RATE_LIMIT_RETRY_DELAY, (e.retryAfterSeconds ?: 0).seconds))
            } catch (e: Exception) {
                logger.warn("Failed to send logs to channel {}: {}...", channelId, e.message?.take(3000))
                return
            }
        }
    }

    private suspend fun send(bot: TgBotInfo, channelId: Long, messageText: String) {
        tgMessageService.sendMessage(bot) {
            chatId = channelId
            text = messageText
        }
    }

    companion object {
        private const val QUEUE_CAPACITY = 10_000
        private const val MAX_MESSAGE_LENGTH = 4000
        private val MIN_RATE_LIMIT_RETRY_DELAY = 1.seconds
    }
}
