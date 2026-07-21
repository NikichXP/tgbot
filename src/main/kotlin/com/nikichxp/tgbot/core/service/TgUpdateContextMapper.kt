package com.nikichxp.tgbot.core.service

import com.nikichxp.tgbot.core.dto.Message
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgUpdateContext
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.entity.common.CallbackModel
import com.nikichxp.tgbot.core.entity.common.MessageModel
import com.nikichxp.tgbot.core.entity.common.ReplyModel
import com.nikichxp.tgbot.core.entity.common.UserModel
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.core.util.getMentionedMessage
import org.springframework.stereotype.Component

@Component
class TgUpdateContextMapper {

    fun mapToUpdateContext(update: Update, bot: TgBotInfo): TgUpdateContext {
        update.bot = bot
        val updateContext = TgUpdateContext(update, bot)
        
        val mentionedMessage = update.getMentionedMessage()

        updateContext.id = update.updateId
        updateContext.from = mapFromEntity(mentionedMessage)
        updateContext.reply = mapReplyEntity(mentionedMessage)
        updateContext.message = mapMessageEntity(mentionedMessage)
        updateContext.callback = mapCallbackEntity(update)
        
        return updateContext
    }

    private fun mapFromEntity(mentionedMessage: Message?): UserModel? {
        return mentionedMessage?.from?.let {
            UserModel(
                id = it.id.toString(),
                username = it.username,
                fullName = listOfNotNull(it.firstName, it.lastName).joinToString(" ")
            )
        }
    }

    private fun mapReplyEntity(mentionedMessage: Message?): ReplyModel? {
        return mentionedMessage?.replyToMessage?.let { replyMessage ->
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
    }

    private fun mapMessageEntity(mentionedMessage: Message?): MessageModel? {
        return mentionedMessage?.messageId?.let { messageId ->
            mentionedMessage.text?.let { text ->
                MessageModel(
                    id = messageId.toString(),
                    text = text
                )
            }
        }
    }

    private fun mapCallbackEntity(update: Update): CallbackModel? {
        return update.callbackQuery?.let { callbackQuery ->
            CallbackModel(
                userId = callbackQuery.from.id,
                data = callbackQuery.data,
                messageText = callbackQuery.message?.text,
                buttonText = callbackQuery.message!!.replyMarkup?.inlineKeyboard?.flatten()
                    ?.find { it.callbackData == callbackQuery.data }?.text!!,
                chatId = update.getContextChatId()!!,
                messageId = callbackQuery.message.messageId
            )
        }
    }
}
