package com.nikichxp.tgbot.entity

import com.nikichxp.tgbot.dto.Update
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class UpdateContext(val update: Update, val tgBot: TgBot) : AbstractCoroutineContextElement(UpdateContext) {
    companion object Key : CoroutineContext.Key<UpdateContext>
}
