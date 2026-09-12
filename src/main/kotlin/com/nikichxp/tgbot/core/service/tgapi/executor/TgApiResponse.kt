package com.nikichxp.tgbot.core.service.tgapi.executor

import com.fasterxml.jackson.databind.JsonNode

data class TgApiResponse(
    val content: JsonNode,
    val success: Boolean = true,
    val responseType: TgResponseType = TgResponseType.OK
)

enum class TgResponseType {
    OK,
    TOO_MANY_REQUESTS,
    MESSAGE_TOO_LONG,
    UNKNOWN_ERROR
}






















