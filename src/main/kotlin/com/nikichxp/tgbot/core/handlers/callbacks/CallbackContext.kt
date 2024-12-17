package com.nikichxp.tgbot.core.handlers.callbacks

import com.nikichxp.tgbot.core.dto.Update

data class CallbackContext(
    var userId: Long,
    var data: String,
    var messageText: String,
    var buttonText: String
) {
    constructor(update: Update) : this(
        userId = update.callbackQuery?.from?.id!!,
        data = update.callbackQuery.data,
        messageText = update.callbackQuery.message?.text!!,
        buttonText = update.callbackQuery.message.replyMarkup?.inlineKeyboard?.flatten()
            ?.find { it.callbackData == update.callbackQuery.data }?.text!!
    )
}