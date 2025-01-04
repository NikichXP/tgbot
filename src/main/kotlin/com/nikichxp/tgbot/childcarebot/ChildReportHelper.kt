package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.core.handlers.callbacks.CallbackContext
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.springframework.stereotype.Service
import java.util.LinkedList

@Service
class ChildReportHelper(
    private val tgOperations: TgOperations,
    private val childInfoService: ChildInfoService,
    private val childActivityService: ChildActivityService
) {

    suspend fun sleepReport(callbackContext: CallbackContext) {
        val child = getChild(callbackContext)
        val activities = childActivityService.getActivities(child.id)
            .sortedBy { it.date }
        val result = LinkedList<String>()

        activities.forEachIndexed { index, activity ->
            if (activity.activity == ChildActivity.SLEEP) {
                val nextActivity = activities.getOrNull(index + 1)
                val duration = nextActivity?.date?.minusMinutes(activity.date.minute.toLong())?.minute
                result.add("From ${activity.date} to ${nextActivity?.date ?: "now"}: $duration minutes")
            }
        }
        
        tgOperations.replyToCurrentMessage("Время сна:\n" + result.joinToString("\n"))
    }

    suspend fun feedingReport(callbackContext: CallbackContext) {

    }

    private fun getChild(callbackContext: CallbackContext): ChildInfo {
        val userId = callbackContext.userId
        return childInfoService.findChildByParent(userId) ?: throw IllegalStateException("Child not found")
    }

}