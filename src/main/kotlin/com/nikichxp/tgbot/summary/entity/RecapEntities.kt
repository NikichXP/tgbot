package com.nikichxp.tgbot.summary.entity

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class RecapOptions(
    val chatId: Long,
    var since: LocalDateTime,
    val model: String? = null
) {

    constructor(chatId: Long, days: Long, model: String? = null) :
            this(chatId, getStartingPointOfDay(LocalDateTime.now().minusDays(days)), model)

    fun getExtraOptionsString(): String {
        val sb = StringBuilder()
        model?.also { modelName -> sb.append("\nМодель: $modelName") }
        sb.append("\nНачиная от: ${since.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")
        return sb.toString()
    }

    companion object {
        private val NIGHT_SEPARATOR = LocalTime.of(4, 0)

        fun ofToday(chatId: Long, model: String? = null): RecapOptions {
            return RecapOptions(chatId, since = getStartingPointOfDay(LocalDateTime.now()), model = model)
        }

        private fun getStartingPointOfDay(dateTime: LocalDateTime): LocalDateTime {
            return if (dateTime.toLocalTime().isBefore(NIGHT_SEPARATOR)) {
                dateTime.minusDays(1).with(NIGHT_SEPARATOR)
            } else {
                dateTime.with(NIGHT_SEPARATOR)
            }
        }
    }

}