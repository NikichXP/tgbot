package com.nikichxp.tgbot.core.error

class NotAuthorizedException : Exception()

class TgApiCallException(message: String) : Exception(message)
