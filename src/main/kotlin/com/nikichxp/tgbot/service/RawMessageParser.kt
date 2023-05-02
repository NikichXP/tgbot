package com.nikichxp.tgbot.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.github.wnameless.json.flattener.JsonFlattener
import com.nikichxp.tgbot.core.CurrentUpdateProvider
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
class RawMessageParser(
    private val objectMapper: ObjectMapper,
    private val mongoTemplate: MongoTemplate,
    private val updateRouter: UpdateRouter,
    private val currentUpdateProvider: CurrentUpdateProvider,
    private val rawJsonLogger: RawJsonLogger
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val ignoreFieldsParser = objectMapper.copy()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun proceedRawData(body: Document, bot: TgBot = TgBot.NIKICHBOT) {
        rawJsonLogger.logEvent(body)
        val source = body.toJson()
        try {
            val (update, diff) = parseUpdateAndGetDiff(source)
            if (diff.isEmpty()) {
                currentUpdateProvider.update = update
                currentUpdateProvider.bot = bot
                updateRouter.proceedUpdate(update)
            } else {
                mongoTemplate.save(UnparsedMessage(body, missedKeys = diff))
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            mongoTemplate.save(UnparsedMessage(body, message = exception.message))
        }
    }

    private fun parseUpdateAndGetDiff(source: String): Pair<Update, Set<String>> {
        val update = ignoreFieldsParser.readValue(source, Update::class.java)
        val control = ignoreFieldsParser.writeValueAsString(update)

        val flatSrc = JsonFlattener.flattenAsMap(source)
        val flatCtr = JsonFlattener.flattenAsMap(control)
        return update to flatSrc.keys.diffWith(flatCtr.keys)
    }

}