package com.nikichxp.tgbot.handlers

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.dto.User
import com.nikichxp.tgbot.entity.InteractionRole
import com.nikichxp.tgbot.entity.MessageInteractionResult
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.entity.UpdateContext
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.entity.UpdateMarker.MESSAGE_IN_GROUP
import com.nikichxp.tgbot.entity.UpdateMarker.HAS_TEXT
import com.nikichxp.tgbot.service.DynamicTextClassifier
import com.nikichxp.tgbot.service.actions.LikedMessageService
import org.springframework.stereotype.Component

@Component
class GroupChatKarmaHandler(
    private val textClassifier: DynamicTextClassifier,
    private val likedMessageService: LikedMessageService
) : UpdateHandler {
    override fun getMarkers(): Set<UpdateMarker> {
        return setOf(HAS_TEXT, MESSAGE_IN_GROUP)
    }

    override fun botSupported(bot: TgBot) = bot == TgBot.NIKICHBOT

    override suspend fun handleUpdate(update: Update) {
        val messageAuthor = getMessageAuthorId(update)
        val replyTarget = getMessageReplyTarget(update) ?: return
        val reaction = textClassifier.classify(update.message!!.text!!)
        val interactionResult = MessageInteractionResult(
            mutableMapOf(
                messageAuthor to InteractionRole.ACTOR, replyTarget to InteractionRole.TARGET
            ), reaction
        )

        if (interactionResult.isLikeInteraction()) {
            likedMessageService.changeRating(interactionResult, update)
        }
    }

    fun getMessageAuthorId(update: Update): User {
        return update.message?.from!!
    }

    fun getMessageReplyTarget(update: Update): User? {
        return update.message?.replyToMessage?.from
    }
}

