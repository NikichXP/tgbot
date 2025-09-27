package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.logic.ChildActivityRepo
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.getContextChatId
import org.springframework.stereotype.Service
import java.time.LocalTime

@Service
class ChildReplyHandler(
    private val childActivityRepo: ChildActivityRepo,
    private val tgOperations: TgOperations
) : UpdateHandler {

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.REPLY)

    override fun requiredFeatures(): Set<String> = setOf(Features.CHILD_TRACKER)

    override suspend fun handleUpdate(update: Update) {
        val text = update.message?.text ?: return
        val time = try {
            getTimeFrom(text)
        } catch (_: Exception) {
            return
        }

        val (chatId, messageId) = update.message.replyToMessage?.let { it.chat.id to it.messageId }
            ?: return replyWithMessage("Debug - Cannot find replied message")

        val activityEvent = childActivityRepo.getActivityByMessageId(chatId, messageId)
            ?: return replyWithMessage("Debug - Cannot find activity for message $messageId in chat $chatId")

        changeEventTime(activityEvent.id, time)

        tgOperations.sendMessage {
            replyToCurrentMessage()
            this.text = "Time updated: $time"
        }

    }

    private suspend fun replyWithMessage(text: String) {
        tgOperations.sendMessage {
            replyToCurrentMessage()
            this.text = text
        }
    }

    private fun changeEventTime(eventId: ChildEventId, newTime: LocalTime) {
        childActivityRepo.updateEvent(eventId) {
            it.date = it.date.with(newTime)
        }
    }

    private fun getTimeFrom(text: String): LocalTime {
        return LocalTime.parse(text)
    }
}