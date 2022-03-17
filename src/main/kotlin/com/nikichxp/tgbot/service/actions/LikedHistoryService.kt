package com.nikichxp.tgbot.service.actions

import com.nikichxp.tgbot.error.DuplicatedRatingError
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
class LikedHistoryService(
    private val mongoTemplate: MongoTemplate
) {

    fun report(actorId: Long, targetId: Long, messageId: Long, power: Double) {
        val report = LikeReport(LikeReportId(actorId, targetId, messageId), power)
        try {
            mongoTemplate.insert(report)
        } catch (e: Exception) {
            throw DuplicatedRatingError()
        }
    }

}

data class LikeReportId(val authorId: Long, val targetId: Long, val messageId: Long)
data class LikeReport(@Id val id: LikeReportId, val power: Double? = null, val date: Long = System.currentTimeMillis())