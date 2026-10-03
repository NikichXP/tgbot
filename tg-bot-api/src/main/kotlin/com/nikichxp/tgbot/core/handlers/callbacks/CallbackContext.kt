package com.nikichxp.tgbot.core.handlers.callbacks

import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.bots.BotInfo
import com.nikichxp.tgbot.core.entity.common.CallbackModel

data class CallbackContext(
    var userId: Long,
    var data: String,
    var messageText: String,
    var buttonText: String?,
    var botInfo: BotInfo,
    var chatId: Long,
    var messageId: Long
) {

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