package com.nikichxp.bot.discord.entity.bots

import com.nikichxp.bot.common.entity.bots.BotInfo
import com.nikichxp.bot.common.entity.bots.BotType

data class DiscordBotInfo(
    val name: String,
    private val supportedFeatures: Set<String>
) : BotInfo {

    override fun getBotType(): BotType = BotType.DISCORD
    override fun getSupportedFeatures(): Set<String> = supportedFeatures

}
