package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.logic.ChildActivityRepo
import com.nikichxp.tgbot.childcarebot.logic.ChildTimezoneService
import com.nikichxp.tgbot.core.dto.Message
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Service
class ChildReplyHandler(
    private val childActivityRepo: ChildActivityRepo,
    private val childTimezoneService: ChildTimezoneService,
    private val tgMessageService: TgMessageService
) : UpdateHandler {

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.REPLY)

    override fun requiredFeatures(): Set<String> = setOf(Features.CHILD_TRACKER)

    override suspend fun handleUpdate(update: Update) {
        val text = update.message?.text ?: return

        when {
            text.matches(TIME_PATTERN.toRegex()) -> {
                updateEventTimeByProvidedTime(text, update.message)
            }

            text.matches(TIME_DIFF_PATTERN.toRegex()) -> {
                updateEventTimeByDiffShift(text, update.message)
            }
            text.matches(ISO_DURATION_PATTERN.toRegex()) -> {
                updateEventTimeByIsoDuration(text, update.message)
            }

            else -> return
        }

    }

    private suspend fun updateEventTimeByProvidedTime(text: String, message: Message) {
        val rawTime = try {
            getTimeFrom(text)
        } catch (_: Exception) {
            return
        }

        val (chatId, messageId) = message.replyToMessage?.let { it.chat.id to it.messageId }
            ?: return replyWithMessage("Debug - Cannot find replied message")

        val activityEvent = childActivityRepo.getActivityByMessageId(chatId, messageId)
            ?: return replyWithMessage("Debug - Cannot find activity for message $messageId in chat $chatId")

        changeEventTime(activityEvent.id, rawTime)

        tgMessageService.sendMessage {
            replyToCurrentMessage()
            this.text = "Time updated: $rawTime"
        }
    }

    private suspend fun updateEventTimeByDiffShift(diff: String, message: Message) {
        val (chatId, messageId) = message.replyToMessage?.let { it.chat.id to it.messageId }
            ?: return replyWithMessage("Debug - Cannot find replied message")

        val activityEvent = childActivityRepo.getActivityByMessageId(chatId, messageId)
            ?: return replyWithMessage("Debug - Cannot find activity for message $messageId in chat $chatId")

        val timeAmount: Long
        val timeUnit: ChronoUnit

        val regex = TIME_DIFF_PATTERN.toRegex()
        val matchResult = regex.find(diff) ?: let {
            tgMessageService.sendMessage {
                replyToCurrentMessage()
                text = "Cannot find data in match result"
            }
            return
        }
        val sign = matchResult.groupValues[1].let { if (it == "-") -1 else 1 }
        val amount = matchResult.groupValues[2].toLong()
        val unit = when (matchResult.groupValues[3]) {
            "min" -> ChronoUnit.MINUTES
            "h" -> ChronoUnit.HOURS
            "hour" -> ChronoUnit.HOURS
            "hours" -> ChronoUnit.HOURS
            else -> throw IllegalArgumentException("Unknown unit: ${matchResult.groupValues[3]}")
        }
        timeAmount = amount * sign
        timeUnit = unit
        var resultDateTime: LocalDateTime? = null

        childActivityRepo.updateEvent(activityEvent.id) {
            resultDateTime = it.date.plus(timeAmount, timeUnit)
            it.date = resultDateTime
        }

        tgMessageService.sendMessage {
            replyToCurrentMessage()
            text = "Edited time: ${childTimezoneService.fromDBToUI(resultDateTime!!)}"
        }
    }

    private suspend fun replyWithMessage(text: String) {
        tgMessageService.sendMessage {
            replyToCurrentMessage()
            this.text = text
        }
    }

    private fun changeEventTime(eventId: ChildEventId, newTime: LocalTime) {
        childActivityRepo.updateEvent(eventId) {
            val uiDateTime = childTimezoneService.fromDBToUI(it.date)
            val newUiDateTime = uiDateTime.with(newTime)
            val newDbDateTime = childTimezoneService.fromUItoDB(newUiDateTime)
            it.date = newDbDateTime
        }
    }

    private fun getTimeFrom(text: String): LocalTime {
        return LocalTime.parse(text)
    }
    
    private suspend fun updateEventTimeByIsoDuration(durationStr: String, message: Message) {
        val (chatId, messageId) = message.replyToMessage?.let { it.chat.id to it.messageId }
            ?: return replyWithMessage("Debug - Cannot find replied message")

        val activityEvent = childActivityRepo.getActivityByMessageId(chatId, messageId)
            ?: return replyWithMessage("Debug - Cannot find activity for message $messageId in chat $chatId")

        val duration = try {
            java.time.Duration.parse(durationStr)
        } catch (e: Exception) {
            return replyWithMessage("Invalid duration format. Use ISO 8601 format like PT1H30M")
        }

        var resultDateTime: LocalDateTime? = null
        childActivityRepo.updateEvent(activityEvent.id) {
            resultDateTime = it.date.plus(duration)
            it.date = resultDateTime!!
        }

        tgMessageService.sendMessage {
            replyToCurrentMessage()
            text = "Edited time: ${childTimezoneService.fromDBToUI(resultDateTime!!)}"
        }
    }

    companion object {
        const val TIME_PATTERN = "\\d{1,2}:\\d{2}"
        const val TIME_DIFF_PATTERN = """^([-+]?)(\d+)\s*(min|h|m|hour|hours)$"""
        const val ISO_DURATION_PATTERN = """^[+-]?PT?(?:\d+H\d*M?|\d+M)$"""
    }
}