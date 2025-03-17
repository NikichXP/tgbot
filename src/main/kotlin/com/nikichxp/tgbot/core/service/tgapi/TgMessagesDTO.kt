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

data class TgReplyParameters(
    @JsonProperty("chat_id") val chatId: Long,
    @JsonProperty("message_id") val messageId: Long
)

interface TgReplyMarkup

data class TgKeyboard(
    val keyboard: List<List<TgButton>>,
    @JsonProperty("resize_keyboard") val resizeKeyboard: Boolean = true,
    @JsonProperty("one_time_keyboard") val oneTimeKeyboard: Boolean = false,
    @JsonProperty("is_persistent") val isPersistent: Boolean = false
): TgReplyMarkup

data class TgInlineKeyboard(
    @JsonProperty("inline_keyboard")
    val inlineKeyboard: List<List<TgInlineButton>>
): TgReplyMarkup {

    companion object {
        fun of(buttons: List<List<Pair<String, String>>>): TgInlineKeyboard {

            return TgInlineKeyboard(inlineKeyboard = buttons.map {
                it.map { (text, callback) ->
                    TgInlineButton(
                        text,
                        callbackData = callback
                    )
                }
            })
        }
    }

}

data class TgInlineButton(
    val text: String,
    val url: String? = null,
    @JsonProperty("callback_data") val callbackData: String? = null
): TgReplyMarkup

data class TgRemoveKeyboard(
    @JsonProperty("remove_keyboard") val removeKeyboard: Boolean = true
): TgReplyMarkup

data class TgButton(
    val text: String,
    @JsonProperty("request_contact")
    val requestContact: Boolean? = null,
    @JsonProperty("request_location")
    val requestLocation: Boolean? = null
)

