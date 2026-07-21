package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents an invite link for a chat.
 * https://core.telegram.org/bots/api#chatinvitelink
 */
data class ChatInviteLink(
    @JsonProperty("invite_link") val inviteLink: String,
    val creator: User,
    @JsonProperty("creates_join_request") val createsJoinRequest: Boolean,
    @JsonProperty("is_primary") val isPrimary: Boolean,
    @JsonProperty("is_revoked") val isRevoked: Boolean,
    val name: String? = null,
    @JsonProperty("expire_date") val expireDate: Long? = null,
    @JsonProperty("member_limit") val memberLimit: Int? = null,
    @JsonProperty("pending_join_request_count") val pendingJoinRequestCount: Int? = null,
    @JsonProperty("subscription_period") val subscriptionPeriod: Int? = null,
    @JsonProperty("subscription_price") val subscriptionPrice: Int? = null
)
