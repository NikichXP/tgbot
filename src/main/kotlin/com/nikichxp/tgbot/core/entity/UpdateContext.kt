package com.nikichxp.tgbot.core.entity

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.bots.TgBot
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class UpdateContext(val update: Update, val tgBot: TgBot) : AbstractCoroutineContextElement(UpdateContext) {
    companion object Key : CoroutineContext.Key<UpdateContext>
}
