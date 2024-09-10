package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.nikichxp.tgbot.core.dto.files.ChatPhoto

/**
 * Represents a chat.
 * https://core.telegram.org/bots/api#chat
 */
data class Chat(
    @JsonProperty("id") val id: Long,
    @JsonProperty("type") val type: String,
    @JsonProperty("title") val title: String? = null,
    @JsonProperty("username") val username: String? = null,
    @JsonProperty("first_name") val firstName: String? = null,
    @JsonProperty("last_name") val lastName: String? = null,
    @JsonProperty("photo") val photo: ChatPhoto? = null,
    @JsonProperty("bio") val bio: String? = null,
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("invite_link") val inviteLink: String? = null,
    @JsonProperty("pinned_message") val pinnedMessage: String? = null,
    @JsonProperty("permissions") val permissions: ChatPermissions? = null,
    @JsonProperty("slow_mode_delay") val slowModeDelay: Int? = null,
    @JsonProperty("sticker_set_name") val stickerSetName: String? = null,
    @JsonProperty("can_set_sticker_set") val canSetStickerSet: Boolean? = null,
    @JsonProperty("linked_chat_id") val linkedChatId: Long? = null,
    @JsonProperty("location") val location: ChatLocation? = null,
)
