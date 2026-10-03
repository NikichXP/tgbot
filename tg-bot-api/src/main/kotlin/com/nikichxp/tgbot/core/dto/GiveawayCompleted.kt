package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a service message about the completion of a giveaway without public winners.
 * https://core.telegram.org/bots/api#giveawaycompleted
 */
data class GiveawayCompleted(
    @JsonProperty("winner_count") val winnerCount: Int,
    @JsonProperty("unclaimed_prize_count") val unclaimedPrizeCount: Int? = null,
    @JsonProperty("giveaway_message") val giveawayMessage: Message? = null,
    @JsonProperty("is_star_giveaway") val isStarGiveaway: Boolean? = null
)
