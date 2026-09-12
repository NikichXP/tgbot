package com.nikichxp.tgbot.core.service.tgapi

import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class TgLastKnownMessageService(
    private val mongoTemplate: MongoTemplate
) {

    private val lastKnownMessageCache = ConcurrentHashMap<String, Long>()
    private val locks = ConcurrentHashMap<String, Any>()

    fun getLastKnownMessage(botInfo: TgBotInfo): BotLastKnownMessage {
        return mongoTemplate.findById<BotLastKnownMessage>(botInfo.name)
            ?: BotLastKnownMessage(botInfo.name, 0)
    }

    fun updateLastKnownMessage(botInfo: TgBotInfo, updateId: Long) {
        val lock = locks.computeIfAbsent(botInfo.name) { Any() }
        synchronized(lock) {
            if (lastKnownMessageCache[botInfo.name] == null || lastKnownMessageCache[botInfo.name]!! < updateId) {
                lastKnownMessageCache[botInfo.name] = updateId
                mongoTemplate.save(BotLastKnownMessage(botInfo.name, updateId, LocalDateTime.now()))
            }
        }
    }

}