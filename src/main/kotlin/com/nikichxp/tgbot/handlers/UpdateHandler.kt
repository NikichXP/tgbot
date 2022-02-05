package com.nikichxp.tgbot.handlers

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.UpdateMarker

interface UpdateHandler {
    fun getMarkers(): Set<UpdateMarker>
    fun handleUpdate(update: Update)
}