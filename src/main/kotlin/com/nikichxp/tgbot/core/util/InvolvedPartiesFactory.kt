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
    MESSAGE_ID("message.id"),
    MESSAGE_TEXT("message.text")
}

data class InvolvedParties(
    val from: UserModel?,
    val reply: ReplyModel?,
    val message: MessageModel?
) {
    fun toFlattenedMap(): Map<MentionedPartyKey, String> {
        val result = mutableMapOf<MentionedPartyKey, String>()
        
        from?.let {
            result[MentionedPartyKey.FROM_ID] = it.id
            it.username?.let { username -> result[MentionedPartyKey.FROM_USERNAME] = username }
            result[MentionedPartyKey.FROM_FULL_NAME] = it.fullName
        }
        
        reply?.let {
            result[MentionedPartyKey.REPLY_TO_ID] = it.id
            it.username?.let { username -> result[MentionedPartyKey.REPLY_TO_USERNAME] = username }
            result[MentionedPartyKey.REPLY_TO_FULL_NAME] = it.fullName
            result[MentionedPartyKey.REPLY_TO_MESSAGE_ID] = it.messageId
            result[MentionedPartyKey.REPLY_TO_TEXT] = it.text
            result[MentionedPartyKey.REPLY_TO_CHAT_ID] = it.chatId
            result[MentionedPartyKey.REPLY_TO_CHAT_TYPE] = it.chatType
            result[MentionedPartyKey.REPLY_TO_CHAT_TITLE] = it.chatTitle
        }
        
        message?.let {
            result[MentionedPartyKey.MESSAGE_ID] = it.id
            result[MentionedPartyKey.MESSAGE_TEXT] = it.text
        }
        
        return result
    }
}

@Component
class InvolvedPartiesFactory {

    fun createInvolvedParties(from: UserModel?, reply: ReplyModel?, message: MessageModel?): InvolvedParties {
        return InvolvedParties(from, reply, message)
    }

    @Deprecated("Use createInvolvedParties with entities directly")
    fun createInvolvedParties(update: Update): InvolvedParties {
        val mentionedMessage = update.getMentionedMessage() ?: return InvolvedParties(null, null, null)
        
        val from = mentionedMessage.from?.let {
            UserModel(
                id = it.id.toString(),
                username = it.username,
                fullName = listOfNotNull(it.firstName, it.lastName).joinToString(" ")
            )
        }
        
        val reply = mentionedMessage.replyToMessage?.let { replyMessage ->
            val replyFrom = replyMessage.from
            if (replyFrom != null) {
                ReplyModel(
                    id = replyFrom.id.toString(),
                    username = replyFrom.username,
                    fullName = listOfNotNull(replyFrom.firstName, replyFrom.lastName).joinToString(" "),
                    messageId = replyMessage.messageId.toString(),
                    text = replyMessage.text ?: "",
                    chatId = replyMessage.chat.id.toString(),
                    chatType = replyMessage.chat.type,
                    chatTitle = replyMessage.chat.title ?: 
                        listOfNotNull(replyMessage.chat.firstName, replyMessage.chat.lastName).joinToString(" ")
                )
            } else null
        }
        
        val message = mentionedMessage.messageId?.let { messageId ->
            mentionedMessage.text?.let { text ->
                MessageModel(
                    id = messageId.toString(),
                    text = text
                )
            }
        }
        
        return InvolvedParties(from, reply, message)
    }

    @Deprecated("Use createInvolvedParties with entities directly")
    fun createInvolvedParties(updateContext: UpdateContext): InvolvedParties {
        return createInvolvedParties(updateContext.getUpdate())
    }
}
