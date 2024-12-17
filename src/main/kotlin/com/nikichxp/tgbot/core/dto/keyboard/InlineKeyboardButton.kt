package com.nikichxp.tgbot.core.dto.keyboard

import com.fasterxml.jackson.annotation.JsonProperty
import com.nikichxp.tgbot.core.dto.CallbackGame

// REWRITED THAT
class InlineKeyboardButton {
    var text: String? = null
    @JsonProperty("callback_data")
    var callbackData: String? = null
}
