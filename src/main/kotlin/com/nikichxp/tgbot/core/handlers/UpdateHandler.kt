package com.nikichxp.tgbot.core.handlers

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateMarker

interface UpdateHandler {
    fun botSupported(bot: TgBot): Boolean
    fun getMarkers(): Set<UpdateMarker>
    suspend fun handleUpdate(update: Update) = Unit
    fun canHandle(update: Update): Boolean = true
}