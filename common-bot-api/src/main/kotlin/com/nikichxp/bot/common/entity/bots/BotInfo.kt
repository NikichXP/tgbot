package com.nikichxp.bot.common.entity.bots

import com.nikichxp.bot.common.handlers.BotFeature

interface BotInfo {

    fun getBotType(): BotType

    fun getSupportedFeatures(): Set<String>

    fun supports(features: Set<BotFeature>): Boolean = getSupportedFeatures().containsAll(features.map { it.id })

}
