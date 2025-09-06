package com.nikichxp.tgbot.core.util

import com.fasterxml.jackson.annotation.JsonIgnore
import com.nikichxp.tgbot.core.dto.Message
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.dto.User
import com.nikichxp.tgbot.core.entity.InteractionRole
import com.nikichxp.tgbot.core.entity.MessageInteractionResult
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.UserId
import kotlinx.coroutines.coroutineScope

suspend fun getCurrentUpdateContext(): UpdateContext = coroutineScope {
    this.coroutineContext[UpdateContext] ?: throw IllegalStateException("No update context found")
}

@JsonIgnore
fun Update.getContextChatId(): Long? = this.getMentionedMessage()?.chat?.id

@JsonIgnore
fun Update.getContextMessageId(): Long? = this.getMentionedMessage()?.messageId

@JsonIgnore
fun Update.getContextUserId(): UserId? = this.getMentionedMessage()?.from?.id

@JsonIgnore
fun Update.getContextUserName(): String? = this.getMentionedMessage()?.from?.username

@JsonIgnore
fun Update.getContextInvolvedParties(): Map<String, String> {
    val mentionedMessage = this.getMentionedMessage() ?: return mapOf()
    val result = mutableMapOf<String, String>()
    mentionedMessage.from?.let {
        result["from.id"] = it.id.toString()
        it.username?.let { username -> result["from.username"] = username }
        result["from.fullName"] = listOfNotNull(it.firstName, it.lastName).joinToString(" ")
    }
    mentionedMessage.replyToMessage?.from?.let {
        result["replyTo.id"] = it.id.toString()
        it.username?.let { username -> result["replyTo.username"] = username }
        result["replyTo.fullName"] = listOfNotNull(it.firstName, it.lastName).joinToString(" ")
    }
    mentionedMessage.replyToMessage?.let {
        result["replyTo.messageId"] = it.messageId.toString()
        result["replyTo.text"] = it.text ?: ""
        result["replyTo.chatId"] = it.chat.id.toString()
        result["replyTo.chatType"] = it.chat.type
        result["replyTo.chatTitle"] = it.chat.title ?:
        listOfNotNull(it.chat.firstName, it.chat.lastName).joinToString(" ")
    }
    mentionedMessage.text?.let {
        result["message.text"] = it
    }
    return result
}

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
