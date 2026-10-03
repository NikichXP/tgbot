package com.nikichxp.tgbot.core.service.tgapi

import com.nikichxp.tgbot.core.entity.bots.TgBotInfo

interface ITgMessageService {

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        replyToMessageId: Long? = null
    )

    suspend fun sendMessage(messageDSL: suspend TgSendMessage.() -> Unit)

    suspend fun sendMessage(tgBot: TgBotInfo, messageDSL: suspend TgSendMessage.() -> Unit)

    suspend fun sendMessage(
        message: TgSendMessage,
        tgBot: TgBotInfo,
    )

    suspend fun replyToCurrentMessage(text: String, replyMarkup: TgReplyMarkup? = null)

    suspend fun editMessageText(
        chatId: Long,
        messageId: Long,
        text: String,
        bot: TgBotInfo,
        replyMarkup: TgReplyMarkup? = null,
    )

    suspend fun editMessageText(
        chatId: Long,
        messageId: Long,
        text: String,
        replyMarkup: TgReplyMarkup? = null,
    )

    suspend fun editMessageText(
        text: String,
        replyMarkup: TgReplyMarkup? = null,
    )

    suspend fun sendDocument(
        chatId: Long,
        bot: TgBotInfo,
        fileName: String,
        fileContent: ByteArray,
        caption: String? = null,
        replyToMessageId: Long? = null
    )
}
