package com.nikichxp.tgbot.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.wnameless.json.flattener.JsonFlattener
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.entity.UnparsedMessage
import com.nikichxp.tgbot.tooling.RawJsonLogger
import com.nikichxp.tgbot.util.diffWith
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service

@Service
class MessageEntryPoint(
    private val objectMapper: ObjectMapper,
    private val mongoTemplate: MongoTemplate,
    private val updateRouter: UpdateRouter,
    private val rawJsonLogger: RawJsonLogger
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun proceedRawData(body: Document, bot: TgBot) {
        logger.info("Received message: $body")
        rawJsonLogger.logEvent(body)
        val update = parseDocumentToUpdate(body)
        proceedUpdate(update, bot)
    }

    fun proceedUpdate(update: Update, bot: TgBot) {
        update.bot = bot
        updateRouter.proceedUpdate(update)
    }

    // TODO can I use Spring-conversions for that?
    private fun parseDocumentToUpdate(body: Document): Update {
        val source = body.toJson()
        try {
            val (update, diff) = parseUpdateAndGetDiff(source)
            if (diff.isEmpty()) {
                return update
            } else {
                mongoTemplate.save(UnparsedMessage(body, missedKeys = diff))
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            mongoTemplate.save(UnparsedMessage(body, message = exception.message))
        }
        throw IllegalArgumentException("Cannot convert the incoming message")
    }

    private fun parseUpdateAndGetDiff(source: String): Pair<Update, Set<String>> {
        val update = objectMapper.readValue(source, Update::class.java)
        val control = objectMapper.writeValueAsString(update)

        val flatSrc = JsonFlattener.flattenAsMap(source)
        val flatCtr = JsonFlattener.flattenAsMap(control)
        return update to flatSrc.keys.diffWith(flatCtr.keys)
    }

}