package com.nikichxp.tgbot.childcarebot.logic

import com.nikichxp.tgbot.childcarebot.ChildActivity
import com.nikichxp.tgbot.childcarebot.ChildActivityEvent
import com.nikichxp.tgbot.childcarebot.ChildEventId
import com.nikichxp.tgbot.childcarebot.TgMessageInfo
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ChildActivityRepo(
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

    fun getActivityByMessageId(chatId: Long, messageId: Long): ChildActivityEvent? {
        return mongoTemplate.findOne(
            Query.query(
                Criteria.where(ChildActivityEvent::sentMessages.name).elemMatch(
                    Criteria.where("chatId").`is`(chatId)
                        .and("messageId").`is`(messageId)
                )
            )
        )
    }

    fun getActivitiesSince(childId: Long, startDate: LocalDateTime): List<ChildActivityEvent> {
        return mongoTemplate.find(
            Query.query(
                Criteria.where(ChildActivityEvent::childId.name).`is`(childId)
                    .and(ChildActivityEvent::date.name).gte(startDate)
            )
        )
    }

    fun getLastEvent(childId: Long): ChildActivityEvent? {
        return mongoTemplate.findOne<ChildActivityEvent>(
            Query(
                Criteria.where(ChildActivityEvent::childId.name).`is`(childId)
            ).with(Sort.by(Sort.Order.desc(ChildActivityEvent::date.name))).limit(1)
        )
    }

    fun getLastEvents(childId: Long, count: Int): List<ChildActivityEvent> {
        return mongoTemplate.find<ChildActivityEvent>(
            Query(
                Criteria.where(ChildActivityEvent::childId.name).`is`(childId)
            ).with(Sort.by(Sort.Order.desc(ChildActivityEvent::date.name))).limit(count)
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

    fun updateEvent(eventId: ChildEventId, updateAction: (ChildActivityEvent) -> Unit) {
        val event = mongoTemplate.findById<ChildActivityEvent>(eventId) ?: return
        updateAction(event)
        mongoTemplate.save(event)
    }

}