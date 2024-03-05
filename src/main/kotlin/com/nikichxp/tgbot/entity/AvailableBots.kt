package com.nikichxp.tgbot.entity

import com.nikichxp.tgbot.config.AppConfig
import org.springframework.context.annotation.Configuration

enum class TgBot(val botName: String) {
    NIKICHBOT("nikichbot"), ALLMYSTUFFBOT("allmystuffbot"), SANTABOT("santabot"),
    DEMOBOT("demobot")
}

data class BotInfo(
    val bot: TgBot,
    val name: String,
    val token: String
)

@Configuration
class TgBotConfig(
    private val appConfig: AppConfig
) {

    fun getBotInfo(bot: TgBot): BotInfo? {
        val token = when(bot) {
            TgBot.NIKICHBOT -> appConfig.tokens.nikichBot
            TgBot.ALLMYSTUFFBOT -> appConfig.tokens.allMyStuffBot
            TgBot.SANTABOT -> appConfig.tokens.santaBot
            TgBot.DEMOBOT -> appConfig.tokens.demoBot
        } ?: return null
        return BotInfo(
            bot = bot,
            name = bot.botName,
            token = token
        )
    }

    fun getInitializedBots(): List<BotInfo> {
        return TgBot.values().mapNotNull { getBotInfo(it) }
    }

}