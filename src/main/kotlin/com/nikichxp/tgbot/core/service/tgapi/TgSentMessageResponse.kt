package com.nikichxp.tgbot.core.service.tgapi

import com.fasterxml.jackson.annotation.JsonProperty

data class TgSentMessageResponse(
    val ok: Boolean,
    val result: TgResult?
)

class TgResult {
    @JsonProperty("message_id")
    var messageId: Long = 0
    @JsonProperty("chat")
    lateinit var chat: TgChatInfo
}

class TgChatInfo {
    @JsonProperty("id")
    var id: Long = 0
}
