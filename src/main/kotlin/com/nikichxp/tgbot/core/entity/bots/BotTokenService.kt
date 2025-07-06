package com.nikichxp.tgbot.core.entity.bots

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Service

@Service
class BotTokenService(
    private val mongoTemplate: MongoTemplate
) {

    fun getBotToken(bot: TgBot): String? {
        return mongoTemplate.findById<BotConfig>(bot.botName)?.token
    }

    // TODO Someday add API to update bot token
    fun saveBotToken(bot: TgBot, token: String) {
        val botConfig = BotConfig(botName = bot.botName, token = token)
        mongoTemplate.save(botConfig)
    }

}