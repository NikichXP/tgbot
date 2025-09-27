package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.logic.ChildActivityRepo
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
    private val tgOperations: TgOperations
) : UpdateHandler {

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.REPLY)

    override fun requiredFeatures(): Set<String> = setOf(Features.CHILD_TRACKER)

    override suspend fun handleUpdate(update: Update) {
        val text = update.message?.text ?: return
        val time = try {
            getTimeFrom(text)
        } catch (e: Exception) {
            return
        }

        tgOperations.sendMessage {
            replyToCurrentMessage()
            this.text = "Time set to $time"
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