package com.nikichxp.tgbot.core.handlers.callbacks

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.bots.BotInfo
import com.nikichxp.tgbot.core.entity.common.CallbackModel
import com.nikichxp.tgbot.core.util.getContextChatId

data class CallbackContext(
    var userId: Long,
    var data: String,
    var messageText: String,
    var buttonText: String,
    var botInfo: BotInfo,
    var chatId: Long,
    var messageId: Long
) {

    constructor(update: Update) : this(
        userId = update.callbackQuery?.from?.id!!,
        data = update.callbackQuery.data,
        messageText = update.callbackQuery.message?.text!!,
        buttonText = update.callbackQuery.message.replyMarkup?.inlineKeyboard?.flatten()
            ?.find { it.callbackData == update.callbackQuery.data }?.text!!,
        botInfo = update.bot,
        chatId = update.getContextChatId() ?: -1L,
        messageId = update.callbackQuery.message.messageId
    )

    constructor(callbackModel: CallbackModel, updateContext: UpdateContext) : this(
        userId = callbackModel.userId,
        data = callbackModel.data,
        messageText = callbackModel.messageText ?: "",
        buttonText = callbackModel.buttonText,
        botInfo = updateContext.getBotInfo(),
        chatId = callbackModel.chatId,
        messageId = callbackModel.messageId
    )

    constructor(updateContext: UpdateContext) : this(
        updateContext.callback ?: throw IllegalArgumentException("No callback found in update context"),
        updateContext
    )

}