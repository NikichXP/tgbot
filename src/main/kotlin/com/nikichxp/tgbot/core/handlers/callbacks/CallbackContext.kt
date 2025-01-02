package com.nikichxp.tgbot.core.handlers.callbacks

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.util.getContextChatId

data class CallbackContext(
    var userId: Long,
    var data: String,
    var messageText: String,
    var buttonText: String,
    var bot: TgBot,
    var chatId: Long
) {
    constructor(update: Update) : this(
        userId = update.callbackQuery?.from?.id!!,
        data = update.callbackQuery.data,
        messageText = update.callbackQuery.message?.text!!,
        buttonText = update.callbackQuery.message.replyMarkup?.inlineKeyboard?.flatten()
            ?.find { it.callbackData == update.callbackQuery.data }?.text!!,
        bot = update.bot,
        chatId = update.getContextChatId() ?: -1L
    )
}