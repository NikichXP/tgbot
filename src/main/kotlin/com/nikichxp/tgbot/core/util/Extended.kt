package com.nikichxp.tgbot.core.util

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateMarker

fun Update.getMarkers(): Set<UpdateMarker> {
    return UpdateMarker.entries.filter {
        val result = it.predicate.apply(this)
        result as? Boolean ?: (result != null)
    }.toSet()
}

fun <T> Collection<T>.diffWith(other: Collection<T>): Set<T> {
    return this.subtract(other.toSet()) + other.subtract(this.toSet())
}