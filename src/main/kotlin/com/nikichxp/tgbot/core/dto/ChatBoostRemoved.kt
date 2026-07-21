package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a boost removed from a chat.
 * https://core.telegram.org/bots/api#chatboostremoved
 */
data class ChatBoostRemoved(
    val chat: Chat,
    @JsonProperty("boost_id") val boostId: String,
    @JsonProperty("remove_date") val removeDate: Long,
    val source: ChatBoostSource
)
