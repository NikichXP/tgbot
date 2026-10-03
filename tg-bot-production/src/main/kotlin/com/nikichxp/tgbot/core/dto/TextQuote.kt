package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Contains information about the quoted part of a message resent because of a reply.
 * https://core.telegram.org/bots/api#textquote
 */
data class TextQuote(
    val text: String,
    val entities: List<MessageEntity>? = null,
    val position: Int,
    @JsonProperty("is_manual") val isManual: Boolean? = null
)
