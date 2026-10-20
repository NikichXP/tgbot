package com.nikichxp.tgbot.core.tooling

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.LoggingEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.LinkedBlockingQueue

class TelegramLogAppenderTest {

    @Test
    fun `maps event to level, logger name, time and message with stack trace`() {
        val queue = LinkedBlockingQueue<BotAppLogEvent>()
        val appender = TelegramLogAppender(queue).apply { start() }
        val logger = LoggerContext().getLogger("com.example.FooService")

        val loggingEvent = LoggingEvent("fqcn", logger, Level.ERROR, "failed {}", IllegalStateException("boom"), arrayOf("x"))

        appender.doAppend(loggingEvent)

        val event = queue.single()
        assertThat(event.level).isEqualTo(Level.ERROR)
        assertThat(event.loggerName).isEqualTo("com.example.FooService")
        assertThat(event.time).isEqualTo(loggingEvent.instant)
        assertThat(event.message)
            .startsWith("failed x\n")
            .contains("java.lang.IllegalStateException: boom")
    }

    @Test
    fun `drops events when queue is full`() {
        val queue = LinkedBlockingQueue<BotAppLogEvent>(1)
        val appender = TelegramLogAppender(queue).apply { start() }
        val logger = LoggerContext().getLogger("foo")

        repeat(3) { appender.doAppend(LoggingEvent("fqcn", logger, Level.INFO, "m$it", null, null)) }

        assertThat(queue.map { it.message }).containsExactly("m0")
    }

    @Test
    fun `formats event as time, level, short logger name and message`() {
        val time = LocalDateTime.of(2026, 1, 2, 3, 4, 5).atZone(ZoneId.systemDefault()).toInstant()
        val event = BotAppLogEvent("hello", Level.WARN, "com.example.FooService", time)

        assertThat(event.toFormattedString()).isEqualTo("03:04:05.000 WARN [FooService] hello")
    }
}
