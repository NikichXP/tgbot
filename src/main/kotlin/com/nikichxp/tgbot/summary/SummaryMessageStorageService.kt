package com.nikichxp.tgbot.summary

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.summary.entity.LoggedMessage
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SummaryMessageStorageService(
    private val mongoTemplate: MongoTemplate
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun storeMessage(update: Update) {
        val chatId = update.getContextChatId()
        if (chatId == null) {
            logger.warn("No chat id in update $update")
            return
        }
        val loggedMessage = LoggedMessage(update = update, chatId = chatId)
        mongoTemplate.save(loggedMessage)
    }

    fun getMessages(chatId: Long): List<LoggedMessage> {
        return mongoTemplate.find<LoggedMessage>(
            Query.query(
                Criteria.where(LoggedMessage::chatId.name).`is`(chatId)
            )
        )
    }

    fun getMessagesAfter(chatId: Long, after: LocalDateTime): List<LoggedMessage> {
        return mongoTemplate.find<LoggedMessage>(
            Query.query(
                Criteria.where(LoggedMessage::chatId.name).`is`(chatId)
                    .and(LoggedMessage::time.name).gt(after)
            )
        )
    }
}