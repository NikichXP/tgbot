package com.nikichxp.tgbot.oauth.repository

import com.nikichxp.tgbot.oauth.entity.OAuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository

@Repository
class OAuthClientRepository(private val mongoTemplate: MongoTemplate) {

    suspend fun findById(clientId: String): OAuthClient? = withContext(Dispatchers.IO) {
        mongoTemplate.findById<OAuthClient>(clientId)
    }

    suspend fun findAll(): List<OAuthClient> = withContext(Dispatchers.IO) {
        mongoTemplate.findAll<OAuthClient>()
    }

    suspend fun insert(client: OAuthClient): OAuthClient = withContext(Dispatchers.IO) {
        mongoTemplate.insert(client)
    }

    suspend fun save(client: OAuthClient): OAuthClient = withContext(Dispatchers.IO) {
        mongoTemplate.save(client)
    }

    suspend fun deleteById(clientId: String): Boolean = withContext(Dispatchers.IO) {
        mongoTemplate.remove<OAuthClient>(Query(Criteria.where("_id").`is`(clientId))).deletedCount > 0
    }
}
