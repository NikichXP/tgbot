package com.nikichxp.tgbot.service

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Service

@Service
class EmojiService(
    private val mongoTemplate: MongoTemplate
) {

    fun getEmojiPower(emoji: String): Double? {
        val info = mongoTemplate.findById<EmojiInfo>(emoji)
        return info?.power
    }

    fun saveEmojiInfo(emoji: String, power: Double): Boolean {
        if (power !in -1.0..1.0) {
            return false
        }
        mongoTemplate.insert(EmojiInfo(emoji, power))
        return true
    }

    fun listEmojis(): List<Pair<String, Double>> {
        return mongoTemplate.findAll<EmojiInfo>().map { it.emoji to it.power }
    }

}

data class EmojiInfo(
    @Id
    val emoji: String,
    val power: Double
)