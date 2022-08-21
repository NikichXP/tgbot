package com.nikichxp.tgbot.util

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.UpdateMarker

fun Update.getMarkers(): Set<UpdateMarker> {
    return UpdateMarker.values().filter { it.predicate.apply(this) != null }.toSet()
}

fun <T> Collection<T>.diffWith(other: Collection<T>): Set<T> {
    return this.subtract(other.toSet()) + other.subtract(this.toSet())
}