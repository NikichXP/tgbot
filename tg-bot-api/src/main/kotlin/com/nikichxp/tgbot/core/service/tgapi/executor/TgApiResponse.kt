package com.nikichxp.tgbot.core.service.tgapi.executor

import com.fasterxml.jackson.databind.JsonNode

data class TgApiResponse(
    val content: JsonNode,
    val success: Boolean = true,
    val responseStatus: TgResponseStatus = TgResponseStatus.OK
)

enum class TgResponseStatus {
    OK,
    TOO_MANY_REQUESTS,
    MESSAGE_TOO_LONG,
    CONFLICT,
    UNKNOWN_ERROR
}






















