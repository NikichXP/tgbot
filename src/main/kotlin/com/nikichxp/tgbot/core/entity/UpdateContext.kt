package com.nikichxp.tgbot.core.entity

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.bots.TgBot
import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class UpdateContext(val update: Update, val tgBot: TgBot) : AbstractCoroutineContextElement(UpdateContext) {

    // so far it's null
    var tgBotV2: TgBotInfoV2? = null

    companion object Key : CoroutineContext.Key<UpdateContext>
}
