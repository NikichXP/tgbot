package com.nikichxp.bot.common.entity

import com.nikichxp.bot.common.entity.bots.BotInfo
import com.nikichxp.bot.common.entity.model.CallbackModel
import com.nikichxp.bot.common.entity.model.ChatModel
import com.nikichxp.bot.common.entity.model.MessageModel
import com.nikichxp.bot.common.entity.model.ReplyModel
import com.nikichxp.bot.common.entity.model.UserModel

interface UpdateContext {

    fun getBotInfo(): BotInfo

    fun getChatId(): Long

    val chat: ChatModel?
    val from: UserModel?
    val reply: ReplyModel?
    val message: MessageModel?
    val callback: CallbackModel?

}
