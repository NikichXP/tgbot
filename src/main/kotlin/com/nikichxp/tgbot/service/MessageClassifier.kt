package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.MessageInteractionResult
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.util.getMarkers
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException

@Component
class MessageClassifier(
    handlers: List<UpdateHandler>
) {

    private val handlerMarkers = handlers.associateBy { it.getMarkers() }

    // TODO code here
    fun proceedUpdate(update: Update) {
        val handler = handlerMarkers[update.getMarkers()]
            ?: throw IllegalArgumentException("cant proceed message cause no handler for ${update.getMarkers()} found")
        handler.getResult(update) // then -> proceed results and give someone some karma
    }

}

interface UpdateHandler {
    fun getMarkers(): Set<UpdateMarker>
    fun getResult(update: Update): MessageInteractionResult
}

@Component
class TextUpdateHandler : UpdateHandler {
    override fun getMarkers(): Set<UpdateMarker> {
        return setOf(UpdateMarker.MESSAGE_WITH_TEXT)
    }

    override fun getResult(update: Update): MessageInteractionResult {

        return MessageInteractionResult("")
    }
}

