package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents changes in the status of a chat member.
 * https://core.telegram.org/bots/api#chatmemberupdated
 */
data class ChatMemberUpdated(
    val chat: Chat,
    val from: User,
    val date: Long,
    @JsonProperty("old_chat_member") val oldChatMember: ChatMember,
    @JsonProperty("new_chat_member") val newChatMember: ChatMember,
    @JsonProperty("invite_link") val inviteLink: ChatInviteLink? = null,
    @JsonProperty("via_join_request") val viaJoinRequest: Boolean? = null,
    @JsonProperty("via_chat_folder_invite_link") val viaChatFolderInviteLink: Boolean? = null
)
