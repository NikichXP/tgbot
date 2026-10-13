package com.nikichxp.tgbot.core.handlers

import com.nikichxp.bot.common.entity.UpdateContext

interface Authenticable {
    suspend fun authenticate(context: UpdateContext): Boolean
}