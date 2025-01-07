package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.core.handlers.callbacks.CallbackContext
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.AppStorage
import org.springframework.stereotype.Service
import java.time.Duration
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
    private val appStorage: AppStorage
) {

    suspend fun sleepReport(callbackContext: CallbackContext) {
        val hostTimeZone = appStorage.getOrPut("timezone.child.host", "UTC+2")
        val userTimeZone = appStorage.getOrPut("timezone.child.user", "UTC+1")

        val child = getChild(callbackContext)
        val startDate = LocalDateTime.now().minusDays(7)
        val activities = childActivityService.getActivitiesSince(child.id, startDate)
            .map {
                it.copy(
                    date = it.date
                        .withSecond(0)
                        .withNano(0)
                        .atZone(ZoneId.of(hostTimeZone))
                        .withZoneSameInstant(ZoneId.of(userTimeZone))
                        .toLocalDateTime()
                )
            }
            .sortedBy { it.date }

        val result = LinkedList<String>()

        val sleeps = mutableListOf<Pair<LocalDateTime, LocalDateTime?>>()

        val now = LocalDateTime.now()

        activities.forEachIndexed { index, activity ->
            if (activity.activity == ChildActivity.SLEEP) {
                val nextActivity = activities.getOrNull(index + 1)
                sleeps.add(activity.date to (nextActivity?.date ?: now))
            }
        }

        var lastDay = activities.minBy { it.date}.date.toLocalDate()

        sleeps
            .map { (a, b) -> (a to b) to (b?.let { formatSleep(a, it) } ?: a.format(longDateFormat)) }
            .forEach { (pair, line) ->
                if (pair.first.toLocalDate() != lastDay) {
                    result.add("\n ${pair.first.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM"))}:")
                    lastDay = pair.first.toLocalDate()
                }
                result.add(line)
            }

//        activities
//            .forEachIndexed { index, activity ->
//                if (activity.activity != ChildActivity.SLEEP) {
//                    return@forEachIndexed
//                }
//
//                val nextActivity = activities.getOrNull(index + 1)
//                val currentActivityStr = activity.date.format(longDateFormat)
//
//                val nextActivityStr = if (nextActivity != null) {
//                    if (nextActivity.date.toLocalDate() == activity.date.toLocalDate()) {
//                        nextActivity.date.format(DateTimeFormatter.ofPattern("HH:mm"))
//                    } else {
//                        nextActivity.date.format(longDateFormat)
//                    }
//                } else {
//                    "now"
//                }
//
//                val duration = nextActivity?.date?.let {
//                    Duration.between(activity.date, it)
//                        .toKotlinDuration().toString()
//                }
//                    ?: "до сих пор"
//
//                result.add("From $currentActivityStr to $nextActivityStr: $duration")
//            }

        tgOperations.replyToCurrentMessage("Время сна:\n" + result.joinToString("\n"))
    }

    private fun formatSleep(from: LocalDateTime, to: LocalDateTime): String {
        val duration = Duration.between(from, to).toKotlinDuration().toString()
        val format = if (from.toLocalDate() == to.toLocalDate()) shortDateFormat else longDateFormat

        return "${from.format(longDateFormat)} - ${to.format(format)} ($duration)"
    }

    suspend fun feedingReport(callbackContext: CallbackContext) {

    }

    private fun getChild(callbackContext: CallbackContext): ChildInfo {
        val userId = callbackContext.userId
        return childInfoService.findChildByParent(userId) ?: throw IllegalStateException("Child not found")
    }

    companion object {
        private val longDateFormat = DateTimeFormatter.ofPattern("dd/MM HH:mm")
        private val shortDateFormat = DateTimeFormatter.ofPattern("HH:mm")
    }

}