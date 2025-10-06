package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.logic.ChildActivityRepo
import com.nikichxp.tgbot.childcarebot.logic.ChildTimezoneService
import com.nikichxp.tgbot.core.dto.Message
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.springframework.stereotype.Service
import java.time.LocalTime

@Service
class ChildReplyHandler(
    private val childActivityRepo: ChildActivityRepo,
    private val childTimezoneService: ChildTimezoneService,
    private val tgOperations: TgOperations
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

        tgOperations.sendMessage {
            replyToCurrentMessage()
            this.text = "Time updated: $rawTime"
        }
    }

    private suspend fun updateEventTimeByDiffShift(diff: String, message: Message) {
        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "This pattern is not yet fixed"
        }
        TODO()
    }

    private suspend fun replyWithMessage(text: String) {
        tgOperations.sendMessage {
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

    companion object {
        private const val TIME_PATTERN = "HH:mm"
        private const val TIME_DIFF_PATTERN = "(-|[+])\\d+(min|h|m|hour|hours)"
    }
}