package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a reaction type. Flattened union of ReactionTypeEmoji/ReactionTypeCustomEmoji/ReactionTypePaid.
 * https://core.telegram.org/bots/api#reactiontype
 */
data class ReactionType(
    val type: String,
    val emoji: String? = null,
    @JsonProperty("custom_emoji_id") val customEmojiId: String? = null
)
