package com.nikichxp.tgbot.core.util

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.common.UserModel
import com.nikichxp.tgbot.core.entity.common.ReplyModel
import com.nikichxp.tgbot.core.entity.common.MessageModel
import org.springframework.stereotype.Component

enum class MentionedPartyKey(val value: String) {
    FROM_ID("from.id"),
    FROM_USERNAME("from.username"),
    FROM_FULL_NAME("from.fullName"),
    REPLY_TO_ID("replyTo.id"),
    REPLY_TO_USERNAME("replyTo.username"),
    REPLY_TO_FULL_NAME("replyTo.fullName"),
    REPLY_TO_MESSAGE_ID("replyTo.messageId"),
    REPLY_TO_TEXT("replyTo.text"),
    REPLY_TO_CHAT_ID("replyTo.chatId"),
    REPLY_TO_CHAT_TYPE("replyTo.chatType"),
    REPLY_TO_CHAT_TITLE("replyTo.chatTitle"),
    MESSAGE_TEXT("message.text")
}

data class InvolvedParties(
    val parties: Map<MentionedPartyKey, String>
) {
    fun get(key: MentionedPartyKey): String? = parties[key]

    fun getFrom(): UserModel? {
        val id = parties[MentionedPartyKey.FROM_ID] ?: return null
        val username = parties[MentionedPartyKey.FROM_USERNAME]
        val fullName = parties[MentionedPartyKey.FROM_FULL_NAME] ?: return null
        return UserModel(id, username, fullName)
    }

    fun getReply(): ReplyModel? {
        val id = parties[MentionedPartyKey.REPLY_TO_ID] ?: return null
        val username = parties[MentionedPartyKey.REPLY_TO_USERNAME]
        val fullName = parties[MentionedPartyKey.REPLY_TO_FULL_NAME] ?: return null
        val messageId = parties[MentionedPartyKey.REPLY_TO_MESSAGE_ID] ?: return null
        val text = parties[MentionedPartyKey.REPLY_TO_TEXT] ?: return null
        val chatId = parties[MentionedPartyKey.REPLY_TO_CHAT_ID] ?: return null
        val chatType = parties[MentionedPartyKey.REPLY_TO_CHAT_TYPE] ?: return null
        val chatTitle = parties[MentionedPartyKey.REPLY_TO_CHAT_TITLE] ?: return null
        return ReplyModel(id, username, fullName, messageId, text, chatId, chatType, chatTitle)
    }

    fun getMessage(): MessageModel? {
        val text = parties[MentionedPartyKey.MESSAGE_TEXT] ?: return null
        return MessageModel(text)
    }
}

@Component
class InvolvedPartiesFactory {

    fun createInvolvedParties(update: Update): InvolvedParties {
        val mentionedMessage = update.getMentionedMessage() ?: return InvolvedParties(mapOf())
        val result = mutableMapOf<MentionedPartyKey, String>()
        
        mentionedMessage.from?.let {
            result[MentionedPartyKey.FROM_ID] = it.id.toString()
            it.username?.let { username -> result[MentionedPartyKey.FROM_USERNAME] = username }
            result[MentionedPartyKey.FROM_FULL_NAME] = listOfNotNull(it.firstName, it.lastName).joinToString(" ")
        }
        
        mentionedMessage.replyToMessage?.from?.let {
            result[MentionedPartyKey.REPLY_TO_ID] = it.id.toString()
            it.username?.let { username -> result[MentionedPartyKey.REPLY_TO_USERNAME] = username }
            result[MentionedPartyKey.REPLY_TO_FULL_NAME] = listOfNotNull(it.firstName, it.lastName).joinToString(" ")
        }
        
        mentionedMessage.replyToMessage?.let {
            result[MentionedPartyKey.REPLY_TO_MESSAGE_ID] = it.messageId.toString()
            result[MentionedPartyKey.REPLY_TO_TEXT] = it.text ?: ""
            result[MentionedPartyKey.REPLY_TO_CHAT_ID] = it.chat.id.toString()
            result[MentionedPartyKey.REPLY_TO_CHAT_TYPE] = it.chat.type
            result[MentionedPartyKey.REPLY_TO_CHAT_TITLE] = it.chat.title ?:
                listOfNotNull(it.chat.firstName, it.chat.lastName).joinToString(" ")
        }
        
        mentionedMessage.text?.let {
            result[MentionedPartyKey.MESSAGE_TEXT] = it
        }
        
        return InvolvedParties(result)
    }

    fun createInvolvedParties(updateContext: UpdateContext): InvolvedParties {
        return createInvolvedParties(updateContext.getUpdate())
    }
}
