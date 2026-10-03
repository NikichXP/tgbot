package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a message about the completion of a giveaway with public winners.
 * https://core.telegram.org/bots/api#giveawaywinners
 */
data class GiveawayWinners(
    val chat: Chat,
    @JsonProperty("giveaway_message_id") val giveawayMessageId: Long,
    @JsonProperty("winners_selection_date") val winnersSelectionDate: Long,
    @JsonProperty("winner_count") val winnerCount: Int,
    val winners: List<User>,
    @JsonProperty("additional_chat_count") val additionalChatCount: Int? = null,
    @JsonProperty("prize_star_count") val prizeStarCount: Int? = null,
    @JsonProperty("premium_subscription_month_count") val premiumSubscriptionMonthCount: Int? = null,
    @JsonProperty("unclaimed_prize_count") val unclaimedPrizeCount: Int? = null,
    @JsonProperty("only_new_members") val onlyNewMembers: Boolean? = null,
    @JsonProperty("was_refunded") val wasRefunded: Boolean? = null,
    @JsonProperty("prize_description") val prizeDescription: String? = null
)
