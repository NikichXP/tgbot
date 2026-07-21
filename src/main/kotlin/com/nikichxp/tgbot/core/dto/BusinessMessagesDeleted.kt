package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Received when messages are deleted from a connected business account.
 * https://core.telegram.org/bots/api#businessmessagesdeleted
 */
data class BusinessMessagesDeleted(
    @JsonProperty("business_connection_id") val businessConnectionId: String,
    val chat: Chat,
    @JsonProperty("message_ids") val messageIds: List<Long>
)
