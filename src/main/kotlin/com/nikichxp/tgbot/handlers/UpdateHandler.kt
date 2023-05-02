package com.nikichxp.tgbot.handlers

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.entity.UpdateMarker

interface UpdateHandler {
    fun botSupported(bot: TgBot): Boolean
    fun getMarkers(): Set<UpdateMarker>
    fun handleUpdate(update: Update)
}