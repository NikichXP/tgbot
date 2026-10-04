package com.nikichxp.tgbot.people.repository

import com.nikichxp.tgbot.people.entity.UserInteraction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class UserInteractionRepository(
    private val mongoTemplate: MongoTemplate
) {

    suspend fun exists(userId: Long, botName: String): Boolean = withContext(Dispatchers.IO) {
        mongoTemplate.exists<UserInteraction>(
            Query(
                Criteria.where(UserInteraction::userId.name).`is`(userId)
                    .and(UserInteraction::botName.name).`is`(botName)
            )
        )
    }

    suspend fun save(interaction: UserInteraction): UserInteraction = withContext(Dispatchers.IO) {
        mongoTemplate.save(interaction)
    }
}
