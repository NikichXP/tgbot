package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a boost added to a chat.
 * https://core.telegram.org/bots/api#chatboost
 */
data class ChatBoost(
    @JsonProperty("boost_id") val boostId: String,
    @JsonProperty("add_date") val addDate: Long,
    @JsonProperty("expiration_date") val expirationDate: Long,
    val source: ChatBoostSource
)
