package com.nikichxp.tgbot.core.service

import com.nikichxp.tgbot.core.dto.Chat
import com.nikichxp.tgbot.core.dto.Message
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.dto.User
import com.nikichxp.tgbot.core.entity.TgUpdateContext
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.entity.common.CallbackModel
import com.nikichxp.tgbot.core.entity.common.ChatModel
import com.nikichxp.tgbot.core.entity.common.MessageModel
import com.nikichxp.tgbot.core.entity.common.ReplyModel
import com.nikichxp.tgbot.core.entity.common.StickerModel
import com.nikichxp.tgbot.core.entity.common.UserModel
import com.nikichxp.tgbot.core.entity.common.VoiceModel
import com.nikichxp.tgbot.core.util.getMarkers
import com.nikichxp.tgbot.core.util.getMentionedMessage
import org.springframework.stereotype.Component

@Component
class TgUpdateContextMapper {

    fun mapToUpdateContext(update: Update, bot: TgBotInfo): TgUpdateContext {
        update.bot = bot
        val updateContext = TgUpdateContext(update, bot)

        val mentionedMessage = update.getMentionedMessage()

        updateContext.id = update.updateId
        updateContext.chat = mentionedMessage?.chat?.let(::mapChat)
        updateContext.from = mapUser(mentionedMessage?.from)
        updateContext.reply = mapReply(mentionedMessage?.replyToMessage)
        updateContext.message = mapMessage(mentionedMessage)
        updateContext.callback = mapCallback(update)
        updateContext.markers = update.getMarkers()

        return updateContext
    }

    private fun mapChat(chat: Chat): ChatModel {
        return ChatModel(
            id = chat.id,
            type = chat.type,
            title = chat.title ?: listOfNotNull(chat.firstName, chat.lastName).joinToString(" ")
        )
    }

    private fun mapUser(user: User?): UserModel? {
        return user?.let {
            UserModel(
                id = it.id,
                username = it.username,
                fullName = listOfNotNull(it.firstName, it.lastName).joinToString(" ")
            )
        }
    }

    private fun mapReply(replyMessage: Message?): ReplyModel? {
        return replyMessage?.let {
            ReplyModel(
                messageId = it.messageId,
                text = it.text,
                from = mapUser(it.from),
                chat = mapChat(it.chat)
            )
        }
    }

    private fun mapMessage(message: Message?): MessageModel? {
        return message?.let {
            MessageModel(
                id = it.messageId,
                text = it.text,
                voice = it.voice?.let { voice ->
                    VoiceModel(
                        fileId = voice.fileId,
                        fileUniqueId = voice.fileUniqueId,
                        duration = voice.duration,
                        mimeType = voice.mimeType
                    )
                },
                sticker = it.sticker?.let { sticker ->
                    StickerModel(fileId = sticker.fileId, emoji = sticker.emoji)
                }
            )
        }
    }

    private fun mapCallback(update: Update): CallbackModel? {
        val callbackQuery = update.callbackQuery ?: return null
        val message = callbackQuery.message ?: return null
        return CallbackModel(
            userId = callbackQuery.from.id,
            data = callbackQuery.data,
            messageText = message.text,
            buttonText = message.replyMarkup?.inlineKeyboard?.flatten()
                ?.find { it.callbackData == callbackQuery.data }?.text,
            chatId = message.chat.id,
            messageId = message.messageId
        )
    }
}
