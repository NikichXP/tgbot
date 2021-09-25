package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.InteractionRole
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

    fun proceedUpdate(update: Update) {
        val handler = handlerMarkers[update.getMarkers()]
            ?: throw IllegalArgumentException("cant proceed message cause no handler for ${update.getMarkers()} found")
        val result = handler.getResult(update) // then -> proceed results and give someone some karma
        // TODO code here
    }

}

interface UpdateHandler {
    fun getMarkers(): Set<UpdateMarker>
    fun getResult(update: Update): MessageInteractionResult
}

@Component
class TextUpdateHandler(
    private val textClassifier: TextClassifier
) : UpdateHandler {
    override fun getMarkers(): Set<UpdateMarker> {
        return setOf(UpdateMarker.MESSAGE_WITH_TEXT)
    }

    override fun getResult(update: Update): MessageInteractionResult {
        val messageAuthor = getMessageAuthorId(update)
        val replyTarget = getMessageReplyTarget(update) ?: return MessageInteractionResult.emptyFrom(messageAuthor)

        val reaction = textClassifier.getReaction(update.message!!.text!!)
        return MessageInteractionResult(
            mutableMapOf(
                messageAuthor to InteractionRole.LIKED,
                replyTarget to InteractionRole.TARGET
            ),
            reaction
        )
    }
}

fun getMessageAuthorId(update: Update): Long {
    return update.message?.from?.id!!
}

fun getMessageReplyTarget(update: Update): Long? {
    return update.message?.replyToMessage?.from?.id
}
