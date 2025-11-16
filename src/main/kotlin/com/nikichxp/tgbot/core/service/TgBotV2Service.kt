package com.nikichxp.tgbot.core.service

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2
import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2Entity
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Service

@Service
class TgBotV2Service(
    private val mongoTemplate: MongoTemplate,
    private val appConfig: AppConfig
) {

    fun getAdminBot(): TgBotInfoV2 {
        return appConfig.adminBot?.let { getBotById(it) } ?: throw IllegalStateException("Admin bot is not configured")
    }

    fun getBotById(botId: String): TgBotInfoV2 {
        return TgBotInfoV2(getBotEntityById(botId))
    }

    fun listBots(): List<TgBotInfoV2> {
        return mongoTemplate.findAll<TgBotInfoV2Entity>().map { TgBotInfoV2(it) }
    }

    fun getTokenById(botId: String): String {
        return getBotEntityById(botId).token
    }

    private fun getBotEntityById(botId: String): TgBotInfoV2Entity {
        return mongoTemplate.findById<TgBotInfoV2Entity>(botId)
            ?: throw IllegalArgumentException("Bot with id $botId not found")
    }

}
