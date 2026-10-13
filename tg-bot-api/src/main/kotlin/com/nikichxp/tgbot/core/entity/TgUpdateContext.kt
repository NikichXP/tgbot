package com.nikichxp.tgbot.core.entity

import com.nikichxp.bot.common.entity.UpdateContext
import com.nikichxp.bot.common.entity.bots.BotInfo
import com.nikichxp.bot.common.entity.model.CallbackModel
import com.nikichxp.bot.common.entity.model.ChatModel
import com.nikichxp.bot.common.entity.model.MessageModel
import com.nikichxp.bot.common.entity.model.ReplyModel
import com.nikichxp.bot.common.entity.model.UserModel
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class TgUpdateContext(
    var tgBotV2: TgBotInfo
) :
    AbstractCoroutineContextElement(TgUpdateContext),
    UpdateContext {

    var updateSeqId: Long = 0

    override var chat: ChatModel? = null
    override var from: UserModel? = null
    override var reply: ReplyModel? = null
    override var message: MessageModel? = null
    override var callback: CallbackModel? = null
    var markers: Set<UpdateMarker> = emptySet()

    companion object Key : CoroutineContext.Key<TgUpdateContext>

    override fun getBotInfo(): BotInfo = tgBotV2
    override fun getChatId(): Long = chat?.id ?: throw IllegalStateException("No chat id found")

}
