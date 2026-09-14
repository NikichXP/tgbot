package com.nikichxp.tgbot.core.entity

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.bots.BotInfo
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.entity.common.CallbackModel
import com.nikichxp.tgbot.core.entity.common.ChatModel
import com.nikichxp.tgbot.core.entity.common.MessageModel
import com.nikichxp.tgbot.core.entity.common.ReplyModel
import com.nikichxp.tgbot.core.entity.common.UserModel
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

interface UpdateContext {

    @Deprecated("for migration purposes only!")
    fun getUpdate(): Update
    fun getBotInfo(): BotInfo

    fun getChatId(): Long

    val chat: ChatModel?
    val from: UserModel?
    val reply: ReplyModel?
    val message: MessageModel?
    val callback: CallbackModel?
    val markers: Set<UpdateMarker>

}

data class TgUpdateContext(
    private val update: Update,
    var tgBotV2: TgBotInfo
) :
    AbstractCoroutineContextElement(TgUpdateContext),
    UpdateContext {

    var id: Long = update.updateId

    override var chat: ChatModel? = null
    override var from: UserModel? = null
    override var reply: ReplyModel? = null
    override var message: MessageModel? = null
    override var callback: CallbackModel? = null
    override var markers: Set<UpdateMarker> = emptySet()

    companion object Key : CoroutineContext.Key<TgUpdateContext>

    // TODO do everything needed to remove this method
    override fun getUpdate(): Update = update
    override fun getBotInfo(): BotInfo = tgBotV2
    override fun getChatId(): Long = chat?.id ?: throw IllegalStateException("No chat id found")

}
