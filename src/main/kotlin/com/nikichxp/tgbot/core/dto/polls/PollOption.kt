package com.nikichxp.tgbot.core.dto.polls

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Contains information about one answer option in a poll.
 * https://core.telegram.org/bots/api#polloption
 */
data class PollOption(
    @JsonProperty(PollFields.TEXT) val text: String,
    @JsonProperty(PollFields.VOTER_COUNT) val voterCount: Int
)
