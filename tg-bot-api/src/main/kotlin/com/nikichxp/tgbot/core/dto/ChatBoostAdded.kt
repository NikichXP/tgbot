package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a service message about a user boosting a chat.
 * https://core.telegram.org/bots/api#chatboostadded
 */
data class ChatBoostAdded(
    @JsonProperty("boost_count") val boostCount: Int
)
