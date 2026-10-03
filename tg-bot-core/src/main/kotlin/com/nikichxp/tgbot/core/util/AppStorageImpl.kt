package com.nikichxp.tgbot.core.util

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Service

@Service
class AppStorageImpl(
    private val mongoTemplate: MongoTemplate
) : IAppStorage {

    override fun saveData(data: AppData) = mongoTemplate.save(data)
    override fun saveData(key: String, value: String) = saveData(AppData(key, value))

    override fun getData(key: String) = mongoTemplate.findById<AppData>(key)

    override fun getOrPut(key: String, defaultValue: String): String {
        return getData(key)?.value ?: defaultValue.also { saveData(key, it) }
    }

}
