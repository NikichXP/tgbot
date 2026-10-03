package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a service message about the creation of a scheduled giveaway.
 * https://core.telegram.org/bots/api#giveawaycreated
 */
data class GiveawayCreated(
    @JsonProperty("prize_star_count") val prizeStarCount: Int? = null
)
