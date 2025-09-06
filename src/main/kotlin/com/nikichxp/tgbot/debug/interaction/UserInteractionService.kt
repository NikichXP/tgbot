package com.nikichxp.tgbot.debug.interaction

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
class UserInteractionService(private val mongoTemplate: MongoTemplate) {

    fun registerUserInteraction(userId: Long, botName: String): Boolean {
        val existing = mongoTemplate.findOne<UserInteraction>(
            Query(
                Criteria.where(UserInteraction::userId.name).`is`(userId)
                    .and(UserInteraction::botName.name).`is`(botName)
            )
        )
        if (existing != null) {
            return false
        } else {
            val interaction = UserInteraction(userId, botName)
            mongoTemplate.save(interaction)
            return true
        }
    }
}