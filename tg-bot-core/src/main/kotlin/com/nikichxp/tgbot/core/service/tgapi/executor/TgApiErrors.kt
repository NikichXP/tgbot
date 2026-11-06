package com.nikichxp.tgbot.core.service.tgapi.executor

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.error.TgApiErrorException
import com.nikichxp.tgbot.core.error.TgBadRequestException
import com.nikichxp.tgbot.core.error.TgConflictException
import com.nikichxp.tgbot.core.error.TgForbiddenException
import com.nikichxp.tgbot.core.error.TgMessageTooLongException
import com.nikichxp.tgbot.core.error.TgNotFoundException
import com.nikichxp.tgbot.core.error.TgServerErrorException
import com.nikichxp.tgbot.core.error.TgTooManyRequestsException
import com.nikichxp.tgbot.core.error.TgUnauthorizedException
import com.nikichxp.tgbot.core.error.TgUnexpectedApiErrorException

fun tgApiErrorFor(method: String, errorCode: Int, description: String): TgApiErrorException = when (errorCode) {
    400 if description.contains("message is too long", ignoreCase = true) ->
        TgMessageTooLongException(method, description)
    400 -> TgBadRequestException(method, description)
    401 -> TgUnauthorizedException(method, description)
    403 -> TgForbiddenException(method, description)
    404 -> TgNotFoundException(method, description)
    409 -> TgConflictException(method, description)
    429 -> TgTooManyRequestsException(method, description)
    in 500..599 -> TgServerErrorException(method, errorCode, description)
    else -> TgUnexpectedApiErrorException(method, errorCode, description)
}

fun tgApiErrorFor(method: String, errorCode: Int, errorBody: String, objectMapper: ObjectMapper): TgApiErrorException {
    val description = runCatching { objectMapper.readTree(errorBody).path("description").asText() }
        .getOrNull()
        ?.takeIf { it.isNotEmpty() }
        ?: errorBody
    return tgApiErrorFor(method, errorCode, description)
}
