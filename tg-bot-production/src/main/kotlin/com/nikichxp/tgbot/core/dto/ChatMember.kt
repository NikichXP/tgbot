package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ChatMember(
    @JsonProperty("user") val user: User,
    @JsonProperty("status") val status: String,
    @JsonProperty("custom_title") val customTitle: String? = null,
    @JsonProperty("is_anonymous") val isAnonymous: Boolean? = null,
    @JsonProperty("until_date") val forceReply: Int? = null,
    @JsonProperty("can_be_edited") val canBeEdited: Boolean? = null,
    @JsonProperty("can_post_messages") val canPostMessages: Boolean? = null,
    @JsonProperty("can_edit_messages") val canEditMessages: Boolean? = null,
    @JsonProperty("can_delete_messages") val canDeleteMessages: Boolean? = null,
    @JsonProperty("can_restrict_members") val canRestrictMembers: Boolean? = null,
    @JsonProperty("can_promote_members") val canPromoteMembers: Boolean? = null,
    @JsonProperty("can_change_info") val canChangeInfo: Boolean? = null,
    @JsonProperty("can_invite_users") val canInviteUsers: Boolean? = null,
    @JsonProperty("can_pin_messages") val canPinMessages: Boolean? = null,
    @JsonProperty("is_member") val isMember: Boolean? = null,
    @JsonProperty("can_send_messages") val canSendMessages: Boolean? = null,
    @JsonProperty("can_send_media_messages") val canSendMediaMessages: Boolean? = null,
    @JsonProperty("can_send_polls") val canSendPolls: Boolean? = null,
    @JsonProperty("can_send_other_messages") val canSendOtherMessages: Boolean? = null,
    @JsonProperty("can_add_web_page_previews") val canAddWebPagePreviews: Boolean? = null
)
