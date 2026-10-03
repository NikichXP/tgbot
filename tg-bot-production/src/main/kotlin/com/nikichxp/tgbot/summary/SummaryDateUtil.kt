package com.nikichxp.tgbot.summary

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object SummaryDateUtil {

    private val NIGHT_SEPARATOR = LocalTime.of(4, 0)
    private val DATE_TIME_SPACE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun getStartingPointOfDay(dateTime: LocalDateTime): LocalDateTime {
        return if (dateTime.toLocalTime().isBefore(NIGHT_SEPARATOR)) {
            dateTime.minusDays(1).with(NIGHT_SEPARATOR)
        } else {
            dateTime.with(NIGHT_SEPARATOR)
        }
    }

    fun atSpecificDay(day: LocalDate): LocalDateTime {
        return day.atTime(NIGHT_SEPARATOR)
    }

    fun parseSince(input: String): LocalDateTime {
        val trimmed = input.trim()

        runCatching { LocalDate.parse(trimmed) }.getOrNull()?.let { return atSpecificDay(it) }

        runCatching { LocalDateTime.parse(trimmed) }.getOrNull()?.let { return it }
        runCatching { LocalDateTime.parse(trimmed, DATE_TIME_SPACE_FORMATTER) }.getOrNull()?.let { return it }

        runCatching { LocalTime.parse(trimmed) }.getOrNull()?.let { return LocalDate.now().atTime(it) }

        throw DateTimeParseException("Cannot parse date/time from '$input'", trimmed, 0)
    }
}