package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.core.entity.UserId
import org.bson.types.ObjectId
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

    var sentMessages = mutableListOf<TgMessageInfo>()
}

class TgMessageInfo(
    val chatId: Long,
    val messageId: Long
)
