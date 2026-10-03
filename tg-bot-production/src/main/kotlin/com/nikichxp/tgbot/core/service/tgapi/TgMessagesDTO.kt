package com.nikichxp.tgbot.core.service.tgapi

import com.fasterxml.jackson.annotation.JsonProperty

interface TgReplyMarkup

data class TgReplyParameters(
    @JsonProperty("chat_id") val chatId: Long,
    @JsonProperty("message_id") val messageId: Long
)

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

