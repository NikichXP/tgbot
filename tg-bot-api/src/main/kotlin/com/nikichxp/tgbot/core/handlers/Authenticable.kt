package com.nikichxp.tgbot.core.handlers

import com.nikichxp.tgbot.core.entity.UpdateContext

interface Authenticable {
    suspend fun authenticate(context: UpdateContext): Boolean
}