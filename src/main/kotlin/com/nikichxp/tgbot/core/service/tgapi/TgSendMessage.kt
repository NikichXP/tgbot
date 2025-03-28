package com.nikichxp.tgbot.core.service.tgapi

import com.fasterxml.jackson.annotation.JsonProperty
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.core.util.getContextMessageId
import com.nikichxp.tgbot.core.util.getCurrentUpdateContext

class TgSendMessage {

    @JsonProperty("chat_id")
    var chatId: Long = 0
    lateinit var text: String
    @JsonProperty("reply_markup")
    var replyMarkup: TgReplyMarkup? = null
    @JsonProperty("reply_parameters")
    var replyParameters: TgReplyParameters? = null

    val callbacks = mutableListOf<(TgSentMessageResponse) -> Unit>()

    suspend fun sendInCurrentChat() {
        val update = getCurrentUpdateContext().update
        this.chatId = update.getContextChatId() ?: throw IllegalArgumentException("Can't get chat id")
    }

    suspend fun replyToCurrentMessage() {
        val update = getCurrentUpdateContext().update
        this.chatId = update.getContextChatId() ?: throw IllegalArgumentException("Can't get chat id")
        this.replyParameters = TgReplyParameters(chatId, update.getContextMessageId() ?: throw IllegalArgumentException("Can't get message id"))
    }

    fun withKeyboard(buttons: List<List<String>>) {
        this.replyMarkup = TgKeyboard(
            buttons.map { it.map { text -> TgButton(text) } }
        )
    }

    fun withKeyboard(keyboard: TgReplyMarkup) {
        this.replyMarkup = keyboard
    }

    fun withInlineKeyboard(buttons: List<List<Pair<String, String>>>) {
        this.replyMarkup = TgInlineKeyboard(
            buttons.map { it.map { (text, callbackData) -> TgInlineButton(text, callbackData = callbackData) } }
        )
    }

    fun removeKeyboard() {
        this.replyMarkup = TgRemoveKeyboard()
    }

    fun withCallback(callback: (TgSentMessageResponse) -> Unit) {
        callbacks.add(callback)
    }

    companion object {
        suspend fun create(builder: suspend TgSendMessage.() -> Unit): TgSendMessage {
            return TgSendMessage().apply { builder() }
        }
    }
}