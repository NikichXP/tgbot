package com.nikichxp.tgbot.core.entity.bots

interface BotInfo {

    fun getBotType(): BotType

    fun getSupportedFeatures(): Set<String>

}