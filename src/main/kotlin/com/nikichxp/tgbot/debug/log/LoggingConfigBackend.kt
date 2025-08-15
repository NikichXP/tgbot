package com.nikichxp.tgbot.debug.log

import org.springframework.stereotype.Service

@Service
class LoggingConfigBackend {
    private val loggingToModeMap = mutableMapOf<Long, Boolean>()

    fun shouldLog(chatId: Long): Boolean = loggingToModeMap.containsKey(chatId)

    fun isAdmin(chatId: Long): Boolean = loggingToModeMap[chatId] == true

    fun setLogging(chatId: Long, enabled: Boolean, admin: Boolean = false) {
        if (enabled) {
            loggingToModeMap[chatId] = admin
        } else {
            loggingToModeMap.remove(chatId)
        }
    }
}