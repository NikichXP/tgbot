package com.nikichxp.tgbot.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class CallbackQuery(
    val id: String,
    val from: User,
    val message: Message? = null,
    @JsonProperty("inline_message_id") val inlineMessageId: String? = null,
    val data: String,
    @JsonProperty("chat_instance") val chatInstance: String
)
