package com.nikichxp.tgbot.core.entity.bots

import com.nikichxp.tgbot.core.handlers.BotFeature

interface BotInfo {

    fun getBotType(): BotType

    fun getSupportedFeatures(): Set<String>

    fun supports(features: Set<BotFeature>): Boolean = getSupportedFeatures().containsAll(features.map { it.id })

}
