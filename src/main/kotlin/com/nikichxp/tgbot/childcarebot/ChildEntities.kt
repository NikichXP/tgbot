package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.state.TransitionDetails
import com.nikichxp.tgbot.core.entity.UserId
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEvent
import java.time.LocalDateTime

data class ChildInfo(
    val id: Long,
    val name: String,
    var parents: Set<UserId> = setOf()
)

data class ChildStateTransition(
    val from: ChildActivity,
    val to: ChildActivity,
    val name: String
)

enum class ChildActivity {
    SLEEP, WAKE_UP, EATING
}

typealias ChildEventId = ObjectId

data class ChildActivityEvent(
    val childId: Long,
    val activity: ChildActivity,
    val date: LocalDateTime
) {
    lateinit var id: ChildEventId

    var state = activity
    var sentMessages = mutableListOf<TgMessageInfo>()
}

data class ChildActivityEventMessage(
    val event: ChildActivityEvent,
    val transitionDetails: TransitionDetails
) : ApplicationEvent(event)

class TgMessageInfo(
    val chatId: Long,
    val messageId: Long
)
