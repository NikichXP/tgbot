package com.nikichxp.tgbot.dashboard.repository

import com.nikichxp.tgbot.dashboard.entity.DashboardSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository

@Repository
class DashboardSessionRepository(
    private val mongoTemplate: MongoTemplate
) {

    suspend fun insert(session: DashboardSession): DashboardSession = withContext(Dispatchers.IO) {
        mongoTemplate.insert(session)
    }

    suspend fun save(session: DashboardSession): DashboardSession = withContext(Dispatchers.IO) {
        mongoTemplate.save(session)
    }

    suspend fun findById(refreshTokenHash: String): DashboardSession? = withContext(Dispatchers.IO) {
        mongoTemplate.findById<DashboardSession>(refreshTokenHash)
    }

    suspend fun deleteById(refreshTokenHash: String) {
        withContext(Dispatchers.IO) {
            mongoTemplate.remove<DashboardSession>(Query(Criteria.where("_id").`is`(refreshTokenHash)))
        }
    }
}
