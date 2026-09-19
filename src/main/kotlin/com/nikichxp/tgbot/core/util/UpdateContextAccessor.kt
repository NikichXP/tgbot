package com.nikichxp.tgbot.core.util

import com.nikichxp.tgbot.core.entity.TgUpdateContext
import com.nikichxp.tgbot.core.entity.UpdateContext
import kotlinx.coroutines.coroutineScope

suspend fun getCurrentUpdateContext(): UpdateContext = coroutineScope {
    this.coroutineContext[TgUpdateContext] ?: throw IllegalStateException("No update context found")
}
