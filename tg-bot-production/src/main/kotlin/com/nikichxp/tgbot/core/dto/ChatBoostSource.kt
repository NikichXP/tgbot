package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Flattened union of ChatBoostSourcePremium/GiftCode/Giveaway.
 * https://core.telegram.org/bots/api#chatboostsource
 */
data class ChatBoostSource(
    val source: String,
    val user: User? = null,
    @JsonProperty("giveaway_message_id") val giveawayMessageId: Long? = null,
    @JsonProperty("is_unclaimed") val isUnclaimed: Boolean? = null,
    @JsonProperty("prize_star_count") val prizeStarCount: Int? = null
)
