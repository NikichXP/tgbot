package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Received when a user purchases paid media offered by a bot.
 * https://core.telegram.org/bots/api#paidmediapurchased
 */
data class PaidMediaPurchased(
    val from: User,
    @JsonProperty("paid_media_payload") val paidMediaPayload: String
)
