package com.nikichxp.tgbot.core.handlers

import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.UpdateMarker

interface UpdateHandler : BotSupportFeature {
    fun getMarkers(): Set<UpdateMarker>
    suspend fun handleUpdate(updateContext: UpdateContext)
    fun canHandle(context: UpdateContext): Boolean = true
}