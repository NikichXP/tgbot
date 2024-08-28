package com.nikichxp.tgbot.entity

import java.util.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class UpdateContextHandler(val context: UpdateContext) : AbstractCoroutineContextElement(UpdateContextHandler) {
    public companion object Key : CoroutineContext.Key<UpdateContextHandler>

    val id = UUID.randomUUID().toString().substring(0..7)
}