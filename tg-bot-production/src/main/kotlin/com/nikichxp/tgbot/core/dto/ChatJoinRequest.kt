package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a join request sent to a chat.
 * https://core.telegram.org/bots/api#chatjoinrequest
 */
data class ChatJoinRequest(
    val chat: Chat,
    val from: User,
    @JsonProperty("user_chat_id") val userChatId: Long,
    val date: Long,
    val bio: String? = null,
    @JsonProperty("invite_link") val inviteLink: ChatInviteLink? = null
)
