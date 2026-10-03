package com.nikichxp.tgbot.discord

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class InputJsonStorage(
    private val mongoTemplate: MongoTemplate
) {

    fun saveJson(json: String, additionalInfo: String? = null) {
        val id = UUID.randomUUID().toString()
        val storedJson = StoredJson(id, json, LocalDateTime.now())
        storedJson.additionalInfo = additionalInfo
        mongoTemplate.save(storedJson)
    }

}

data class StoredJson(
    val id: String,
    val json: String,
    val date: LocalDateTime
) {
    var additionalInfo: String? = null
}
