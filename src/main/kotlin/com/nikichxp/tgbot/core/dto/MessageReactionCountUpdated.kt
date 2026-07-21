package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents reaction changes on a message with anonymous reactions.
 * https://core.telegram.org/bots/api#messagereactioncountupdated
 */
data class MessageReactionCountUpdated(
    val chat: Chat,
    @JsonProperty("message_id") val messageId: Long,
    val date: Long,
    val reactions: List<ReactionCount>
)
