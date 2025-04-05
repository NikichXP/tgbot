package com.nikichxp.tgbot.childcarebot.logic

import com.nikichxp.tgbot.childcarebot.ChildActivity
import com.nikichxp.tgbot.childcarebot.ChildInfo
import com.nikichxp.tgbot.childcarebot.getDurationStringBetween
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackContext
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.AppStorage
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.LinkedList

@Service
class ChildReportHelper(
    private val tgOperations: TgOperations,
    private val childInfoRepo: ChildInfoRepo,
    private val childActivityRepo: ChildActivityRepo,
    private val appStorage: AppStorage
) {

    suspend fun sleepReport(callbackContext: CallbackContext) {
        val hostTimeZone = appStorage.getOrPut("timezone.child.host", "UTC+2")
        val userTimeZone = appStorage.getOrPut("timezone.child.user", "UTC+1")

        val child = getChild(callbackContext)
        val startDate = LocalDateTime.now().minusDays(7)
        val activities = childActivityRepo.getActivitiesSince(child.id, startDate)
            .map {
                it.copy(
                    date = convertDate(it.date, hostTimeZone, userTimeZone)
                        .withSecond(0)
                        .withNano(0)
                )
            }
            .sortedBy { it.date }

        val result = LinkedList<String>()

        val sleeps = mutableListOf<Pair<LocalDateTime, LocalDateTime?>>()

        val now = ZonedDateTime.now().withSecond(0).withNano(0)
            .withZoneSameInstant(ZoneId.of(userTimeZone)).toLocalDateTime()

        activities.forEachIndexed { index, activity ->
            if (activity.activity == ChildActivity.SLEEP) {
                val nextActivity = activities.getOrNull(index + 1)
                sleeps.add(activity.date to (nextActivity?.date ?: now))
            }
        }

        var lastDay = activities.minBy { it.date }.date.toLocalDate()

        sleeps
            .map { (a, b) -> (a to b) to (b?.let { formatSleep(a, it) } ?: a.format(longDateFormat)) }
            .forEach { (pair, line) ->
                if (pair.first.toLocalDate() != lastDay) {
                    result.add("\n ${pair.first.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM"))}:")
                    lastDay = pair.first.toLocalDate()
                }
                result.add(line)
            }

        tgOperations.replyToCurrentMessage("Время сна:\n" + result.joinToString("\n"))
    }

    private fun convertDate(date: LocalDateTime, from: String, to: String): LocalDateTime {
        return date
            .atZone(ZoneId.of(from))
            .withZoneSameInstant(ZoneId.of(to))
            .toLocalDateTime()
    }

    private fun formatSleep(from: LocalDateTime, to: LocalDateTime): String {
        val duration = getDurationStringBetween(from, to)
        val format = if (from.toLocalDate() == to.toLocalDate()) shortDateFormat else longDateFormat

        return "${from.format(longDateFormat)} - ${to.format(format)} ($duration)"
    }

    suspend fun feedingReport(callbackContext: CallbackContext) {
        tgOperations.replyToCurrentMessage("not implemented yet")
    }

    private fun getChild(callbackContext: CallbackContext): ChildInfo {
        val userId = callbackContext.userId
        return childInfoRepo.findChildByParent(userId) ?: throw IllegalStateException("Child not found")
    }

    companion object {
        private val longDateFormat = DateTimeFormatter.ofPattern("dd/MM HH:mm")
        private val shortDateFormat = DateTimeFormatter.ofPattern("HH:mm")
    }

}