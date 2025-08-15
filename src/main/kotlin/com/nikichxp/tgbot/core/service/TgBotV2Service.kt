package com.nikichxp.tgbot.core.service

import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Service

@Service
class TgBotV2Service(
    private val mongoTemplate: MongoTemplate
) {

    fun getBotById(botId: String): TgBotInfoV2 {
        return mongoTemplate.findById(botId) ?: throw IllegalArgumentException("Bot with id $botId not found")
    }

    fun listBots(): List<TgBotInfoV2> {
        return mongoTemplate.findAll()
    }

    fun getTokenById(botId: String): String {
        return mongoTemplate.findById<TgBotInfoV2>(botId)?.token ?: throw IllegalArgumentException("Bot with id $botId not found")
    }

}
