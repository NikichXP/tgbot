package com.nikichxp.bot.discord.repository

import com.nikichxp.bot.discord.entity.StoredJson
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class StoredJsonRepository(
    private val mongoTemplate: MongoTemplate
) {

    fun saveJson(json: String, additionalInfo: String? = null) {
        val storedJson = StoredJson(UUID.randomUUID().toString(), json, LocalDateTime.now())
        storedJson.additionalInfo = additionalInfo
        mongoTemplate.save(storedJson)
    }

}
