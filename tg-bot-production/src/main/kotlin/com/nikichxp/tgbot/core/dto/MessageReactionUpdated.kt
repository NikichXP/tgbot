package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a change of a reaction on a message performed by a user.
 * https://core.telegram.org/bots/api#messagereactionupdated
 */
data class MessageReactionUpdated(
    val chat: Chat,
    @JsonProperty("message_id") val messageId: Long,
    val user: User? = null,
    @JsonProperty("actor_chat") val actorChat: Chat? = null,
    val date: Long,
    @JsonProperty("old_reaction") val oldReaction: List<ReactionType>,
    @JsonProperty("new_reaction") val newReaction: List<ReactionType>
)
