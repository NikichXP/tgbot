package com.nikichxp.tgbot.core.entity

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class UpdateContext(val update: Update, var tgBotV2: TgBotInfoV2) : AbstractCoroutineContextElement(UpdateContext) {

    companion object Key : CoroutineContext.Key<UpdateContext>
    
}
