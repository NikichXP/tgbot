package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Describes the connection of the bot with a business account.
 * https://core.telegram.org/bots/api#businessconnection
 */
data class BusinessConnection(
    val id: String,
    val user: User,
    @JsonProperty("user_chat_id") val userChatId: Long,
    val date: Long,
    @JsonProperty("can_reply") val canReply: Boolean? = null,
    @JsonProperty("is_enabled") val isEnabled: Boolean
)
