package com.nikichxp.tgbot.handlers

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.entity.UpdateContext
import com.nikichxp.tgbot.entity.UpdateMarker

interface UpdateHandler {
    fun botSupported(bot: TgBot): Boolean
    fun getMarkers(): Set<UpdateMarker>
    suspend fun handleUpdate(context: UpdateContext) = handleUpdate(context.update)
    suspend fun handleUpdate(update: Update) = Unit
    fun canHandle(context: UpdateContext): Boolean = true
}