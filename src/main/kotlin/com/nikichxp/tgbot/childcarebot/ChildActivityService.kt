package com.nikichxp.tgbot.childcarebot

import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ChildActivityService(
    private val mongoTemplate: MongoTemplate
) {

    fun addActivity(childId: Long, activity: ChildActivity): ChildActivityEvent {
        val event = ChildActivityEvent(childId, activity, LocalDateTime.now())
        mongoTemplate.save(event)
        return event
    }

    fun getActivities(childId: Long): List<ChildActivityEvent> {
        return mongoTemplate.find(Query.query(Criteria.where(ChildActivityEvent::childId.name).`is`(childId)));
    }

    fun getActivitiesSince(childId: Long, startDate: LocalDateTime): List<ChildActivityEvent> {
        return mongoTemplate.find(
            Query.query(
                Criteria.where(ChildActivityEvent::childId.name).`is`(childId)
                    .and(ChildActivityEvent::date.name).gte(startDate)
            )
        );
    }

    fun getLatestState(): ChildActivity {
        val lastActivity = mongoTemplate.findOne<ChildActivityEvent>(
            Query().with(Sort.by(Sort.Order.desc(ChildActivityEvent::date.name))).limit(1)
        )
        return lastActivity?.activity ?: ChildActivity.WAKE_UP
    }

    fun addMessageToEvent(eventId: ChildEventId, chatId: Long, messageId: Long) {
        val event = mongoTemplate.findById<ChildActivityEvent>(eventId) ?: return
        event.sentMessages += TgMessageInfo(chatId = chatId, messageId = messageId)
        mongoTemplate.save(event) // TODO update operation with push?
    }

}