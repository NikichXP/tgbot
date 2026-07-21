package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a message about a scheduled giveaway.
 * https://core.telegram.org/bots/api#giveaway
 */
data class Giveaway(
    val chats: List<Chat>,
    @JsonProperty("winners_selection_date") val winnersSelectionDate: Long,
    @JsonProperty("winner_count") val winnerCount: Int,
    @JsonProperty("only_new_members") val onlyNewMembers: Boolean? = null,
    @JsonProperty("has_public_winners") val hasPublicWinners: Boolean? = null,
    @JsonProperty("prize_description") val prizeDescription: String? = null,
    @JsonProperty("country_codes") val countryCodes: List<String>? = null,
    @JsonProperty("prize_star_count") val prizeStarCount: Int? = null,
    @JsonProperty("premium_subscription_month_count") val premiumSubscriptionMonthCount: Int? = null
)
