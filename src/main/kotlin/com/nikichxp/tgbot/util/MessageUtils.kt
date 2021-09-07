package com.nikichxp.tgbot.util

import com.nikichxp.tgbot.dto.Update
import kotlin.reflect.full.declaredMemberProperties

fun Update.listUpdated(): List<String> {
    return this::class.declaredMemberProperties
        .filter { it.call(this) != null }
        .map { it.name }
}

fun Any.listNotNullFields(): List<String> {
    return this::class.declaredMemberProperties
        .filter { it.call(this) != null }
        .map { it.name }
}