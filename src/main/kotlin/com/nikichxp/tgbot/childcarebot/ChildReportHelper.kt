package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.core.handlers.callbacks.CallbackContext
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.LinkedList
import kotlin.time.toKotlinDuration

@Service
class ChildReportHelper(
    private val tgOperations: TgOperations,
    private val childInfoService: ChildInfoService,
    private val childActivityService: ChildActivityService,
) {

    suspend fun sleepReport(callbackContext: CallbackContext) {
        val child = getChild(callbackContext)
        val startDate = LocalDateTime.now().minusDays(7)
        val activities = childActivityService.getActivitiesSince(child.id, startDate)
            .map {
                it.copy(
                    date = it.date
                        .withSecond(0)
                        .withNano(0)
                        .atZone(ZoneId.of("UTC+2")) // TODO save timezone in db
                        .withZoneSameInstant(ZoneId.of("UTC+1")) // TODO get zone from config
                        .toLocalDateTime()
                )
            }
            .sortedBy { it.date }

        val result = LinkedList<String>()

        var lastDayChecked = LocalDate.of(1, 1, 1)
        activities
            .forEachIndexed { index, activity ->
                if (activity.activity != ChildActivity.SLEEP) {
                    return@forEachIndexed
                }
                if (activity.date.toLocalDate() != lastDayChecked) {
                    lastDayChecked = activity.date.toLocalDate()
                    result.add("\n")
                }

                val nextActivity = activities.getOrNull(index + 1)
                val currentActivityStr = activity.date.format(longDateFormat)

                val nextActivityStr = if (nextActivity != null) {
                    if (nextActivity.date.toLocalDate() == activity.date.toLocalDate()) {
                        nextActivity.date.format(DateTimeFormatter.ofPattern("HH:mm"))
                    } else {
                        nextActivity.date.format(longDateFormat)
                    }
                } else {
                    "now"
                }

                val duration = nextActivity?.date?.let {
                    Duration.between(activity.date, it)
                        .toKotlinDuration().toString()
                }
                    ?: "ongoing"

                result.add("From $currentActivityStr to $nextActivityStr: $duration")
            }

        tgOperations.replyToCurrentMessage("Время сна:\n" + result.joinToString("\n"))
    }

    suspend fun feedingReport(callbackContext: CallbackContext) {

    }

    private fun getChild(callbackContext: CallbackContext): ChildInfo {
        val userId = callbackContext.userId
        return childInfoService.findChildByParent(userId) ?: throw IllegalStateException("Child not found")
    }

    companion object {
        private val longDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }

}