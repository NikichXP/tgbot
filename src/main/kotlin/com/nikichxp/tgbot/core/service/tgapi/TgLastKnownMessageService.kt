package com.nikichxp.tgbot.core.service.tgapi

import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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

    fun getLastKnownMessage(botInfo: TgBotInfoV2): BotLastKnownMessage {
        return mongoTemplate.findById<BotLastKnownMessage>(botInfo.name)
            ?: BotLastKnownMessage(botInfo.name, 0)
    }


    suspend fun updateLastKnownMessage(botInfo: TgBotInfoV2, updateId: Long) {
        coroutineScope {
            launch {
                synchronized(botInfo.name.intern()) {
                    if (lastKnownMessageCache[botInfo.name] == null || lastKnownMessageCache[botInfo.name]!! < updateId) {
                        lastKnownMessageCache[botInfo.name] = updateId
                        mongoTemplate.save(BotLastKnownMessage(botInfo.name, updateId, LocalDateTime.now()))
                    }
                }
            }
        }
    }

}