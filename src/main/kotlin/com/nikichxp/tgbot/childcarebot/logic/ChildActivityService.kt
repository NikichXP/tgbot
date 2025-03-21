package com.nikichxp.tgbot.childcarebot.logic

import com.nikichxp.tgbot.childcarebot.ChildActivity
import com.nikichxp.tgbot.childcarebot.ChildActivityEvent
import com.nikichxp.tgbot.childcarebot.ChildEventId
import com.nikichxp.tgbot.childcarebot.TgMessageInfo
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.findDistinct
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.collections.plusAssign

@Service
class ChildActivityService(
    private val mongoTemplate: MongoTemplate,
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

    @Deprecated("use with id")
    fun getLatestState(): ChildActivity {
        val lastActivity = mongoTemplate.findOne<ChildActivityEvent>(
            Query().with(Sort.by(Sort.Order.desc(ChildActivityEvent::date.name))).limit(1)
        )
        return lastActivity?.activity ?: ChildActivity.WAKE_UP
    }

    fun getLastEvent(childId: Long): ChildActivityEvent? {
        return mongoTemplate.findOne<ChildActivityEvent>(
            Query(
                Criteria.where(ChildActivityEvent::childId.name).`is`(childId)
            ).with(Sort.by(Sort.Order.desc(ChildActivityEvent::date.name))).limit(1)
        )
    }

    fun addMessageToEvent(eventId: ChildEventId, chatId: Long, messageId: Long) {
        val event = mongoTemplate.findById<ChildActivityEvent>(eventId) ?: return
        event.sentMessages += TgMessageInfo(chatId = chatId, messageId = messageId)
        mongoTemplate.save(event) // TODO update operation with push?
    }

    fun getAllChildrenThatHasEvents(): Collection<Long> {
        return mongoTemplate.findDistinct<Long, ChildActivityEvent>(Query(), "childId")
    }

}