package com.nikichxp.tgbot.core.tooling

import ch.qos.logback.classic.Level
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class BotAppLogEvent(
    val message: String,
    val level: Level,
    val loggerName: String,
    val time: Instant
) {

    fun toFormattedString(): String =
        "${TIME_FORMAT.format(time)} $level [${loggerName.substringAfterLast('.')}] $message"

    companion object {
        private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault())
    }
}
