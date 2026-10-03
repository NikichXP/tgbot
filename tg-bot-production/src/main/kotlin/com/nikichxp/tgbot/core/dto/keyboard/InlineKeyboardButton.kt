package com.nikichxp.tgbot.core.dto.keyboard

import com.fasterxml.jackson.annotation.JsonProperty

// REWRITED THAT
class InlineKeyboardButton {
    var text: String? = null
    @JsonProperty("callback_data")
    var callbackData: String? = null
}
