package com.nikichxp.tgbot.core.entity.bots

data class TgBotInfoV2(var name: String, var supportedFeatures: Set<String>) {

    constructor(bot: TgBotInfoV2Entity) : this(bot.name, bot.supportedFeatures)

    fun getCorrespondingLegacyBot(): TgBot {
        return TgBot.valueOf(name)
    }

}