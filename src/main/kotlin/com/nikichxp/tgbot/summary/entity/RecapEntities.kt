package com.nikichxp.tgbot.summary.entity

import com.nikichxp.tgbot.summary.SummaryDateUtil
import com.nikichxp.tgbot.summary.SummaryDateUtil.getStartingPointOfDay
import java.time.LocalDate
import java.time.LocalDateTime
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
        fun ofToday(chatId: Long, model: String? = null): RecapOptions {
            return RecapOptions(chatId, since = SummaryDateUtil.atSpecificDay(LocalDate.now()), model = model)
        }
    }
}

class RecapOptionsBuilder {

    lateinit var since: LocalDateTime
    var days: Int = -1
    var model: String? = null

    fun hasSince() = ::since.isInitialized

    fun build(chatId: Long): RecapOptions {
        return when {
            hasSince() -> RecapOptions(chatId = chatId, since = since, model = model)
            days > 0 -> RecapOptions(chatId = chatId, days = days.toLong(), model = model)
            else -> RecapOptions.ofToday(chatId, model)
        }
    }

}
