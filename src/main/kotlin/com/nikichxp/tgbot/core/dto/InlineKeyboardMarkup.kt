package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.nikichxp.tgbot.core.dto.keyboard.InlineKeyboardButton

data class InlineKeyboardMarkup internal constructor(
    @JsonProperty("inline_keyboard") val inlineKeyboard: List<List<InlineKeyboardButton>>
) : ReplyMarkup
