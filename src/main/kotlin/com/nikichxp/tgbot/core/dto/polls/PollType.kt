package com.nikichxp.tgbot.core.dto.polls

import com.fasterxml.jackson.annotation.JsonProperty

enum class PollType {
    @JsonProperty("quiz") QUIZ,
    @JsonProperty("regular") REGULAR;
}
