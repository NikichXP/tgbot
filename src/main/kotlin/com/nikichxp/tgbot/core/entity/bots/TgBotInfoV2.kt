package com.nikichxp.tgbot.core.entity.bots

data class TgBotInfoV2(var name: String, var supportedFeatures: Set<String>) {

    var updateFetchType: UpdateFetchType = UpdateFetchType.WEBHOOK

    constructor(bot: TgBotInfoV2Entity) : this(bot.name, bot.supportedFeatures)

}

enum class UpdateFetchType {
    POLLING,
    WEBHOOK
}