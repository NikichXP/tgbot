package com.nikichxp.tgbot.core.entity

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.util.AppStorage
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

enum class TgBot(val botName: String) {
    NIKICHBOT("nikichbot"), ALLMYSTUFFBOT("allmystuffbot"), SANTABOT("santabot"),
    DEMOBOT("demobot"), CHILDTRACKERBOT("childtrackerbot")
}

data class BotInfo(
    val bot: TgBot,
    val name: String,
    val token: String
)

@Service
class TgBotProvider(
    private val appConfig: AppConfig,
    private val appStorage: AppStorage
) {

    private val botMap = ConcurrentHashMap<TgBot, CachedBotInfo>()

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    fun cleanCache() {
        val now = LocalDateTime.now()
        botMap.entries.removeIf { it.value.expire.isBefore(now) }
    }

    fun getBotInfo(bot: TgBot): BotInfo? {
        return botMap.getOrPut(bot) { CachedBotInfo(computeBotInfo(bot)!!) }.botInfo
    }

    fun getInitializedBots(): List<BotInfo> {
        return TgBot.values().mapNotNull { getBotInfo(it) }
    }

    private fun computeBotInfo(bot: TgBot): BotInfo? {

        val token = when (bot) {
            TgBot.NIKICHBOT -> appConfig.tokens.nikichBot
            TgBot.ALLMYSTUFFBOT -> appConfig.tokens.allMyStuffBot
            TgBot.SANTABOT -> appConfig.tokens.santaBot
            TgBot.DEMOBOT -> appConfig.tokens.demoBot
            TgBot.CHILDTRACKERBOT -> appConfig.tokens.childTrackerBot
        } ?: return null

        return BotInfo(
            bot = bot,
            name = bot.botName,
            token = token
        )
    }

    private data class CachedBotInfo(
        val botInfo: BotInfo
    ) {
        val expire: LocalDateTime = LocalDateTime.now().plusMinutes(5)
    }

    companion object {
        private const val BOT_TEMPLATE = "bot.token.%s"
    }

}
