package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.ChildActivity.*
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.time.LocalDateTime

data class ChildStateTransition(
    val from: ChildActivity,
    val to: ChildActivity,
    val name: String
)

@Service
class ChildStateTransitionService {

    private val transitions = mutableSetOf<ChildStateTransition>()

    @PostConstruct
    fun init() {
        transition(SLEEP, WAKE_UP, "Проснулась")
        transition(WAKE_UP, SLEEP, "Уснула")
        transition(WAKE_UP, EATING, "Кушает")
        transition(EATING, WAKE_UP, "Доела")
    }

    private fun transition(from: ChildActivity, to: ChildActivity, name: String) {
        transitions.add(ChildStateTransition(from, to, name))
    }

    fun getStateText(state: ChildActivity): String {
        return when (state) {
            SLEEP -> "Спит"
            WAKE_UP -> "Бодрствует"
            EATING -> "Кушает"
        }
    }

    fun getPossibleTransitions(from: ChildActivity): Map<ChildActivity, String> {
        return transitions
            .filter { it.from == from }
            .associate { it.to to it.name }
    }

    fun getResultState(from: ChildActivity, action: String): ChildActivity? {
        return transitions.find { it.from == from && it.name == action }?.to
    }

}

enum class ChildActivity {
    SLEEP, WAKE_UP, EATING
}

data class ChildActivityEvent(
    val activity: ChildActivity,
    val date: LocalDateTime
) {
    lateinit var id: ObjectId
}

@Service
class ChildActivityService(
    private val mongoTemplate: MongoTemplate
) {

    fun addActivity(activity: ChildActivity) {
        val event = ChildActivityEvent(activity, LocalDateTime.now())
        mongoTemplate.save(event)
    }

    fun getActivities(): List<ChildActivityEvent> {
        return mongoTemplate.findAll()
    }

    fun getLatestState(): ChildActivity {
        val lastActivity = mongoTemplate.findOne<ChildActivityEvent>(
            Query().with(Sort.by(Sort.Order.desc(ChildActivityEvent::date.name))).limit(1)
        )
        return lastActivity?.activity ?: WAKE_UP
    }

}