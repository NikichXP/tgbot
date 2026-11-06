package com.nikichxp.tgbot.core.error

sealed class TgApiErrorException(
    val method: String,
    val errorCode: Int,
    val description: String
) : RuntimeException("Telegram API $method failed with $errorCode: $description")

open class TgBadRequestException(
    method: String,
    description: String
) : TgApiErrorException(method, 400, description)

class TgMessageTooLongException(
    method: String,
    description: String
) : TgBadRequestException(method, description)

class TgUnauthorizedException(
    method: String,
    description: String
) : TgApiErrorException(method, 401, description)

class TgForbiddenException(
    method: String,
    description: String
) : TgApiErrorException(method, 403, description)

class TgNotFoundException(
    method: String,
    description: String
) : TgApiErrorException(method, 404, description)

class TgConflictException(
    method: String,
    description: String
) : TgApiErrorException(method, 409, description)

class TgTooManyRequestsException(
    method: String,
    description: String
) : TgApiErrorException(method, 429, description)

class TgServerErrorException(
    method: String,
    errorCode: Int,
    description: String
) : TgApiErrorException(method, errorCode, description)

class TgUnexpectedApiErrorException(
    method: String,
    errorCode: Int,
    description: String
) : TgApiErrorException(method, errorCode, description)
