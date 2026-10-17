package com.nikichxp.tgbot.core.tooling

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.LoggingEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.LinkedBlockingQueue

class TelegramLogAppenderTest {

    @Test
    fun `formats event with level, short logger name, message and stack trace`() {
        val queue = LinkedBlockingQueue<String>()
        val appender = TelegramLogAppender(queue).apply { start() }
        val logger = LoggerContext().getLogger("com.example.FooService")

        appender.doAppend(LoggingEvent("fqcn", logger, Level.ERROR, "failed {}", IllegalStateException("boom"), arrayOf("x")))

        assertThat(queue.single())
            .startsWith("ERROR FooService: failed x\n")
            .contains("java.lang.IllegalStateException: boom")
    }

    @Test
    fun `drops events when queue is full`() {
        val queue = LinkedBlockingQueue<String>(1)
        val appender = TelegramLogAppender(queue).apply { start() }
        val logger = LoggerContext().getLogger("foo")

        repeat(3) { appender.doAppend(LoggingEvent("fqcn", logger, Level.INFO, "m$it", null, null)) }

        assertThat(queue).containsExactly("INFO foo: m0")
    }
}
