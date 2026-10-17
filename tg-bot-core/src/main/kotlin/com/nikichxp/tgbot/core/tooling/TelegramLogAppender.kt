package com.nikichxp.tgbot.core.tooling

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import ch.qos.logback.core.AppenderBase
import java.util.concurrent.BlockingQueue

class TelegramLogAppender(
    private val queue: BlockingQueue<String>
) : AppenderBase<ILoggingEvent>() {

    override fun append(event: ILoggingEvent) {
        val throwable = event.throwableProxy?.let { "\n" + ThrowableProxyUtil.asString(it) }.orEmpty()
        queue.offer("${event.level} ${event.loggerName.substringAfterLast('.')}: ${event.formattedMessage}$throwable")
    }
}
