package com.nikichxp.tgbot.core.entity.bots

data class TgBotInfo(var name: String, private val supportedFeatures: Set<String>): BotInfo {

    var updateFetchType: TgUpdateFetchType = TgUpdateFetchType.WEBHOOK

    constructor(bot: TgBotInfoV2Entity) : this(bot.name, bot.supportedFeatures) {
        this.updateFetchType = bot.updateFetchType
    }

    override fun getBotType(): BotType = BotType.TG
    override fun getSupportedFeatures(): Set<String> = this.supportedFeatures

}
