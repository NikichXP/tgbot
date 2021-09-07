package com.nikichxp.tgbot.dto

import com.fasterxml.jackson.annotation.JsonProperty

class ReplyKeyboardRemove(
    @JsonProperty("remove_keyboard")
    val removeKeyboard: Boolean = true,
    val selective: Boolean? = null
) : ReplyMarkup