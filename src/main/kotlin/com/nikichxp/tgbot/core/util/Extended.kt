package com.nikichxp.tgbot.core.util

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateMarker

fun Update.getMarkers(): Set<UpdateMarker> {
    return UpdateMarker.entries.filter {
        val result = it.predicate.apply(this)
        result as? Boolean ?: (result != null)
    }.toSet()
}