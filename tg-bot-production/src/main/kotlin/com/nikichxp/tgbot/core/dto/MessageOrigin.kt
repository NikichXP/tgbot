package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Flattened union of MessageOriginUser/HiddenUser/Chat/Channel.
 * https://core.telegram.org/bots/api#messageorigin
 */
data class MessageOrigin(
    val type: String,
    val date: Long,
    @JsonProperty("sender_user") val senderUser: User? = null,
    @JsonProperty("sender_user_name") val senderUserName: String? = null,
    @JsonProperty("sender_chat") val senderChat: Chat? = null,
    val chat: Chat? = null,
    @JsonProperty("message_id") val messageId: Long? = null,
    @JsonProperty("author_signature") val authorSignature: String? = null
)
