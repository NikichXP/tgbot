package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty as Name

data class ChosenInlineResult(
    @Name("result_id") val resultId: String,
    val from: User,
    val location: Location? = null,
    @Name("inline_message_id") val inlineMessageId: String? = null,
    val query: String
)
