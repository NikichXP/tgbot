package com.nikichxp.tgbot.summary

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.util.AppStorage
import com.nikichxp.tgbot.core.util.getContextChatId
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
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

@Service
class SummaryService(
    private val messageDAO: MessageDAO,
    private val appStorage: AppStorage
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Cacheable("summary_feature")
    fun getFeatureEnabledStatus(chatId: Long): Boolean {
        return appStorage.getData("summary_feature_$chatId")?.value?.toBoolean() ?: false
    }

    @CacheEvict("summary_feature", allEntries = true)
    fun setFeatureEnabledStatus(chatId: Long, enabled: Boolean) {
        appStorage.saveData("summary_feature_$chatId", enabled.toString())
    }

    fun saveUpdate(update: Update) {
        val chatId = update.getContextChatId()
        if (chatId == null) {
            logger.warn("No chat id in update $update")
            return
        }
        messageDAO.storeMessage(LoggedMessage(update = update, chatId = chatId))
    }

    fun getUpdatesForChat(chatId: Long): List<LoggedMessage> {
        return messageDAO.getMessages(chatId)
    }
}