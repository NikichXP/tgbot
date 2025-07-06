package com.nikichxp.tgbot.core.entity.bots

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class TgBotProvider(
    private val botTokenService: BotTokenService
) {

    private val botMap = ConcurrentHashMap<TgBot, Pair<BotInfo?, LocalDateTime>>()

    @Scheduled(cron = "0 * * * * *")
    fun cleanCache() {
        val now = LocalDateTime.now()
        botMap.entries.removeIf { it.value.second.isBefore(now) }
    }

    fun getBotInfo(bot: TgBot): BotInfo? {
        return botMap.getOrPut(bot) { computeBotInfo(bot) to LocalDateTime.now() }?.first
    }

    fun getInitializedBots(): List<BotInfo> {
        return TgBot.entries.mapNotNull { getBotInfo(it) }
    }

    private fun computeBotInfo(bot: TgBot): BotInfo? {
        val token = botTokenService.getBotToken(bot) ?: return null

        return BotInfo(
            bot = bot,
            name = bot.botName,
            token = token
        )
    }

}