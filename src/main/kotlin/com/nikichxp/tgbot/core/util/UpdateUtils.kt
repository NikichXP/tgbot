package com.nikichxp.tgbot.core.util

import com.fasterxml.jackson.annotation.JsonIgnore
import com.nikichxp.tgbot.core.dto.Message
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.dto.User
import com.nikichxp.tgbot.core.entity.InteractionRole
import com.nikichxp.tgbot.core.entity.MessageInteractionResult

@JsonIgnore
fun Update.getContextChatId(): Long? = this.getMentionedMessage()?.chat?.id

@JsonIgnore
fun Update.getContextMessageId(): Long? = this.getMentionedMessage()?.messageId

@JsonIgnore
fun Update.getContextUserId(): Long? = this.getMentionedMessage()?.from?.id

@JsonIgnore
fun Update.getContextUserName(): String? = this.getMentionedMessage()?.from?.username

@JsonIgnore
fun Update.getMentionedMessage(): Message? {
    return this.message
        ?: this.editedMessage
        ?: this.editedChannelPost
        ?: this.channelPost
        ?: this.callbackQuery?.message
}

@JsonIgnore
fun Update.getMembers(): MembersOfUpdate? = this.getMentionedMessage()?.let {
    MembersOfUpdate(
        author = it.from,
        target = it.replyToMessage?.from
    )
}

fun Update.convertToMessageIntResult(power: Double): MessageInteractionResult? {
    val messageAuthor = this.getMentionedMessage()?.from ?: return null
    val replyTarget = this.getMentionedMessage()?.replyToMessage?.from ?: return null
    return MessageInteractionResult(
        mutableMapOf(
            messageAuthor to InteractionRole.ACTOR, replyTarget to InteractionRole.TARGET
        ), power
    )
}

data class MembersOfUpdate(val author: User?, val target: User?)
