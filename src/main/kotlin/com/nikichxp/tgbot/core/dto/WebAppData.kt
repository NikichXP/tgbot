package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Describes data sent from a Web App to the bot.
 * https://core.telegram.org/bots/api#webappdata
 */
data class WebAppData(
    val data: String,
    @JsonProperty("button_text") val buttonText: String
)
