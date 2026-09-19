package com.nikichxp.tgbot.summary

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.dto.User
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.common.UserModel
import com.nikichxp.tgbot.core.util.UserFormatter
import com.nikichxp.tgbot.core.util.getMentionedMessage
import com.nikichxp.tgbot.summary.entity.LoggedMessage
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SummaryMessageStorageService(
    private val mongoTemplate: MongoTemplate,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val collectionName by lazy { mongoTemplate.getCollectionName(LoggedMessage::class.java) }

    fun storeMessage(context: UpdateContext) {
        val chatId = context.chat?.id
        if (chatId == null) {
            logger.warn("No chat id in update context $context")
            return
        }
        mongoTemplate.save(LoggedMessage.fromContext(context))
    }

    fun getMessages(chatId: Long): List<LoggedMessage> {
        return findMessages(
            Query.query(
                Criteria.where(LoggedMessage::chatId.name).`is`(chatId)
            )
        )
    }

    fun getMessagesAfter(chatId: Long, after: LocalDateTime): List<LoggedMessage> {
        return findMessages(
            Query.query(
                Criteria.where(LoggedMessage::chatId.name).`is`(chatId)
                    .and(LoggedMessage::time.name).gt(after)
            )
        )
    }

    private fun findMessages(query: Query): List<LoggedMessage> {
        return mongoTemplate.find(query, Document::class.java, collectionName)
            .map(::toLoggedMessage)
    }

    private fun toLoggedMessage(document: Document): LoggedMessage {
        migrateLegacyUpdate(document)
        return mongoTemplate.converter.read(LoggedMessage::class.java, document)
    }

    private fun migrateLegacyUpdate(document: Document) {
        val legacyUpdate = document.remove("update") as? Document ?: return
        val update = objectMapper.readValue(legacyUpdate.toJson(), Update::class.java)
        val message = update.getMentionedMessage()
        val reply = message?.replyToMessage

        document["authorId"] = message?.from?.id
        document["authorName"] = message?.from?.let { UserFormatter.getUserPrintName(it.toUserModel()) }
        document["text"] = message?.text ?: message?.sticker?.emoji ?: ""
        document["replyAuthorName"] = reply?.from?.let { UserFormatter.getUserPrintName(it.toUserModel()) }
        document["replyText"] = reply?.text
    }

    private fun User.toUserModel() = UserModel(
        id = id,
        username = username,
        fullName = listOfNotNull(firstName, lastName).joinToString(" ")
    )
}