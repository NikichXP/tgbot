package com.nikichxp.tgbot.summary.entity

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.util.getContextChatId
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class LoggedMessage(
    val update: Update,
    val chatId: Long = update.getContextChatId() ?: throw IllegalArgumentException("Can't get chat id"),
    val time: LocalDateTime = LocalDateTime.now()
) {
    @Id
    lateinit var id: ObjectId
}