package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

class ReplyKeyboardRemove(
    @JsonProperty("remove_keyboard")
    val removeKeyboard: Boolean = true,
    val selective: Boolean? = null
) : ReplyMarkup