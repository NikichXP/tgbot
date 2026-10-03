package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a reaction added to a message along with the number of times it was added.
 * https://core.telegram.org/bots/api#reactioncount
 */
data class ReactionCount(
    val type: ReactionType,
    @JsonProperty("total_count") val totalCount: Int
)
