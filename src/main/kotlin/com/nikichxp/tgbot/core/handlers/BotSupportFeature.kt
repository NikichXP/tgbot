package com.nikichxp.tgbot.core.handlers

import com.nikichxp.tgbot.core.entity.TgBot

interface BotSupportFeature {

    fun supportedBots(): Set<TgBot>

    fun isBotSupported(tgBot: TgBot): Boolean {
        return supportedBots().contains(tgBot)
    }

}