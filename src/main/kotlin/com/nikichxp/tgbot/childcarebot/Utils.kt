package com.nikichxp.tgbot.childcarebot

import java.time.Duration
import java.time.LocalDateTime

fun getDurationStringBetween(from: LocalDateTime, to: LocalDateTime): String {
    val duration = Duration.between(from, to)
    var result = StringBuilder()
    if (duration.toHours() > 0) {
        result = result.append("${duration.toHours()}h ")
    }
    result = result.append("${duration.toMinutesPart()}m")
    return result.toString()
}