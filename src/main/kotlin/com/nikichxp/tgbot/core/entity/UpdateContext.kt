package com.nikichxp.tgbot.core.entity

import com.nikichxp.tgbot.core.dto.Message
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.bots.BotInfo
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

interface UpdateContext {

    @Deprecated("for migration purposes only!")
    fun getUpdate(): Update
    fun getBotInfo(): BotInfo

    fun getChatId(): Long

}

data class TgUpdateContext(private val update: Update, var tgBotV2: TgBotInfo) :
    AbstractCoroutineContextElement(TgUpdateContext),
    UpdateContext {

    companion object Key : CoroutineContext.Key<TgUpdateContext>

    override fun getUpdate(): Update = update
    override fun getBotInfo(): BotInfo = tgBotV2
    override fun getChatId(): Long = getContextChatId() ?: throw IllegalStateException("No chat id found")

    fun getContextChatId(): Long? = getMentionedMessage()?.chat?.id
    fun getContextUserId(): Long? = getMentionedMessage()?.from?.id
    fun getContextMessageId(): Long? = getMentionedMessage()?.messageId

    fun getMentionedMessage(): Message? {
        return this.update.message
            ?: this.update.editedMessage
            ?: this.update.editedChannelPost
            ?: this.update.channelPost
            ?: this.update.callbackQuery?.message
    }

}
