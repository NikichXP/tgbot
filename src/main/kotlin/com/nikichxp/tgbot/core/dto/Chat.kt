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
    @JsonProperty("is_forum") val isForum: Boolean? = null,
    @JsonProperty("photo") val photo: ChatPhoto? = null,
    @JsonProperty("active_usernames") val activeUsernames: List<String>? = null,
    @JsonProperty("birthdate") val birthdate: String? = null,
    @JsonProperty("emoji_status_custom_emoji_id") val emojiStatusCustomEmojiId: String? = null,
    @JsonProperty("emoji_status_expiration_date") val emojiStatusExpirationDate: Long? = null,
    @JsonProperty("bio") val bio: String? = null,
    @JsonProperty("has_private_forwards") val hasPrivateForwards: Boolean? = null,
    @JsonProperty("has_restricted_voice_and_video_messages") val hasRestrictedVoiceAndVideoMessages: Boolean? = null,
    @JsonProperty("join_to_send_messages") val joinToSendMessages: Boolean? = null,
    @JsonProperty("join_by_request") val joinByRequest: Boolean? = null,
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("invite_link") val inviteLink: String? = null,
    @JsonProperty("pinned_message") val pinnedMessage: Message? = null,
    @JsonProperty("permissions") val permissions: ChatPermissions? = null,
    @JsonProperty("slow_mode_delay") val slowModeDelay: Int? = null,
    @JsonProperty("unrestrict_boost_count") val unrestrictBoostCount: Int? = null,
    @JsonProperty("message_auto_delete_time") val messageAutoDeleteTime: Int? = null,
    @JsonProperty("has_aggressive_anti_spam_enabled") val hasAggressiveAntiSpamEnabled: Boolean? = null,
    @JsonProperty("has_hidden_members") val hasHiddenMembers: Boolean? = null,
    @JsonProperty("has_protected_content") val hasProtectedContent: Boolean? = null,
    @JsonProperty("has_visible_history") val hasVisibleHistory: Boolean? = null,
    @JsonProperty("sticker_set_name") val stickerSetName: String? = null,
    @JsonProperty("can_set_sticker_set") val canSetStickerSet: Boolean? = null,
    @JsonProperty("custom_emoji_sticker_set_name") val customEmojiStickerSetName: String? = null,
    @JsonProperty("linked_chat_id") val linkedChatId: Long? = null,
    @JsonProperty("location") val location: ChatLocation? = null,
    @JsonProperty("accent_color_id") val accentColorId: Int? = null,
    @JsonProperty("background_custom_emoji_id") val backgroundCustomEmojiId: String? = null,
    @JsonProperty("profile_accent_color_id") val profileAccentColorId: Int? = null,
    @JsonProperty("profile_background_custom_emoji_id") val profileBackgroundCustomEmojiId: String? = null,
)
