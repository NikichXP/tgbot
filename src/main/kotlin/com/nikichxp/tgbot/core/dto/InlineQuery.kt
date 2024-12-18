package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class InlineQuery(
    val id: String,
    val from: User,
    val location: Location? = null,
    val query: String,
    val offset: String,
    @JsonProperty("chat_type")
    val chatType: String
)
