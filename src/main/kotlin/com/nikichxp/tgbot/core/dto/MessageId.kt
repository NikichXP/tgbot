package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class MessageId(
    @JsonProperty("message_id") val messageId: Long
)
