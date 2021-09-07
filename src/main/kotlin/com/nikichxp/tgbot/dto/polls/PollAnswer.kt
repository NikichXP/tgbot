package com.nikichxp.tgbot.dto.polls

import com.nikichxp.tgbot.dto.User
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents an answer of a user in a non-anonymous poll.
 * https://core.telegram.org/bots/api#poll_answer
 */
data class PollAnswer(
    @JsonProperty(PollFields.POLL_ID) val pollId: String,
    @JsonProperty(PollFields.USER) val user: User,
    @JsonProperty(PollFields.OPTION_IDS) val optionIds: List<Int>
)
