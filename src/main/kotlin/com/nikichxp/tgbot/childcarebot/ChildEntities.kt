package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.core.dto.ChatId
import com.nikichxp.tgbot.core.entity.UserId
import com.sun.nio.sctp.MessageInfo
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

data class ChildActivityEvent(
    val childId: Long,
    val activity: ChildActivity,
    val date: LocalDateTime
) {
    lateinit var id: ObjectId

    var relatedMessages = mutableListOf<TgMessageInfo>()
}

class TgMessageInfo(
    val chatId: Long,
    val messageId: Long
)
