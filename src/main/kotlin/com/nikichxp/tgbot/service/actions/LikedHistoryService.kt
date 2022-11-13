package com.nikichxp.tgbot.service.actions

import com.nikichxp.tgbot.error.DuplicatedRatingError
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class LikedHistoryService(
    private val mongoTemplate: MongoTemplate
) {

    fun filter(
        startDate: Long? = null,
        endDate: Long? = null,
        byActor: Long? = null,
        byTarget: Long? = null
    ): List<LikeReport> {
        val criteria = mapOf<Any?, (Any) -> Criteria>(
            startDate to { Criteria.where("date").gte(it) },
            endDate to { Criteria.where("date").lte(it) },
            byActor to { Criteria.where("_id.authorId").`is`(it) },
            byTarget to { Criteria.where("_id.targetId").`is`(it) }
        ).filterKeys { it != null }.toList()
            .map { it.second(it.first!!) }
            .reduce { a, b -> a.andOperator(b) }
        return mongoTemplate.find(Query(criteria))
    }

    fun report(actorId: Long, targetId: Long, messageId: Long, power: Double) {
        val report = LikeReport(LikeReportId(actorId, targetId, messageId), power)
        try {
            mongoTemplate.insert(report)
        } catch (e: Exception) {
            throw DuplicatedRatingError()
        }
    }

}

data class LikeReportFilter(
    val startDate: Long? = null,
    val endDate: Long? = null,
    val byActor: Long? = null,
    val byTarget: Long? = null
) {

}

data class LikeReport(
    @Id val id: LikeReportId,
    var power: Double,
    var date: Long = System.currentTimeMillis(),
    var source: String? = null
)

data class LikeReportId(val authorId: Long, val targetId: Long, val messageId: Long)
