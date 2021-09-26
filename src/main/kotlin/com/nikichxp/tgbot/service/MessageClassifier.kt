package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.dto.User
import com.nikichxp.tgbot.entity.InteractionRole
import com.nikichxp.tgbot.entity.InteractionType
import com.nikichxp.tgbot.entity.MessageInteractionResult
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.service.actions.LikedMessageService
import com.nikichxp.tgbot.util.getMarkers
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException

@Component
class MessageClassifier(
    handlers: List<UpdateHandler>,
    private val likedMessageService: LikedMessageService
) {

    private val handlerMarkers = handlers.associateBy { it.getMarkers() }

    fun proceedUpdate(update: Update) {
        val handler = handlerMarkers[update.getMarkers()]
            ?: throw IllegalArgumentException("cant proceed message cause no handler for ${update.getMarkers()} found")
        val result = handler.getResult(update) // then -> proceed results and give someone some karma

        // тут какой прикол, сейчас же я всё делаю чисто для бота с лайками
        // потому для MVP нужен только этот функционал
        // но потом я тут добавлю определение хендлеров и всё такое что бы делать разные штуки
        // СНАЧАЛА MVP

        when (result.interactionType) {
            InteractionType.RATING -> likedMessageService.changeRating(result)
            InteractionType.NONE -> {
                /* nothing to do */
            }
        }
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

fun getMessageAuthorId(update: Update): User {
    return update.message?.from!!
}

fun getMessageReplyTarget(update: Update): User? {
    return update.message?.replyToMessage?.from
}
