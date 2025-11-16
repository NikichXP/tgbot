package com.nikichxp.tgbot.core.entity.bots

data class TgBotInfoV2(var name: String, var supportedFeatures: Set<String>) {

    var updateFetchType: TgUpdateFetchType = TgUpdateFetchType.WEBHOOK

    constructor(bot: TgBotInfoV2Entity) : this(bot.name, bot.supportedFeatures) {
        this.updateFetchType = bot.updateFetchType
    }

}
