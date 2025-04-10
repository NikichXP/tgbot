package com.nikichxp.tgbot.core.util

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.stereotype.Service

@Service
class AppStorage(
    private val mongoTemplate: MongoTemplate
) {

    fun saveData(data: AppData) = mongoTemplate.save(data)
    fun saveData(key: String, value: String) = saveData(AppData(key, value))

    fun getData(key: String) = mongoTemplate.findById<AppData>(key)

    fun getOrPut(key: String, defaultValue: String): String {
        return getData(key)?.value ?: defaultValue.also { saveData(key, it) }
    }

}

@Document("app_data")
data class AppData(
    @Id val key: String,
    val value: String
)
