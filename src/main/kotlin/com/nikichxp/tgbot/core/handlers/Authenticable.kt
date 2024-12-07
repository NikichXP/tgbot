package com.nikichxp.tgbot.core.handlers

import com.nikichxp.tgbot.core.dto.Update

interface Authenticable {
    suspend fun authenticate(update: Update): Boolean
}