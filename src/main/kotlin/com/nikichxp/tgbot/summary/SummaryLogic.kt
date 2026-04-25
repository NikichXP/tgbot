package com.nikichxp.tgbot.summary

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.util.getContextChatId
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.time.LocalDateTime

data class LoggedMessage(
    val update: Update,
    val chatId: Long = update.getContextChatId() ?: throw IllegalArgumentException("Can't get chat id"),
    val time: LocalDateTime = LocalDateTime.now()
) {
    @Id
    lateinit var id: ObjectId
}

@Service
class MessageDAO(
    private val mongoTemplate: MongoTemplate
) {

    fun storeMessage(loggedMessage: LoggedMessage) {
        mongoTemplate.save(loggedMessage)
    }

    fun getMessages(chatId: Long): List<LoggedMessage> {
        return mongoTemplate.find<LoggedMessage>(
            Query.query(
                Criteria.where(LoggedMessage::chatId.name).`is`(chatId)
            )
        )
    }
}
