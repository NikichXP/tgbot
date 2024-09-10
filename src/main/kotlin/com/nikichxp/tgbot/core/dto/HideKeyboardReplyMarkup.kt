package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty as Name

data class HideKeyboardReplyMarkup(
    @Name("hide_keyboard") val hideKeyboard: Boolean = true,
    val selective: Boolean? = null
) : ReplyMarkup