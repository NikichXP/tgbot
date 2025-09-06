package com.nikichxp.tgbot.core.converters

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.wnameless.json.flattener.JsonFlattener
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UnparsedMessage
import com.nikichxp.tgbot.core.entity.UnparsedMessageEvent
import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2
import com.nikichxp.tgbot.core.util.diffWith
import org.bson.Document
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service

@Service
class DocumentToUpdateConverter(
    private val objectMapper: ObjectMapper,
    private val mongoTemplate: MongoTemplate,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    fun convert(body: Document, tgBot: TgBotInfoV2): Update? {
        val source = body.toJson()
        try {
            val (update, diff) = parseUpdateAndGetDiff(source)
            if (diff.isEmpty()) {
                return update
            } else {
                processUnparsed(UnparsedMessage(body, missedKeys = diff, bot = tgBot))
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            processUnparsed(UnparsedMessage(body, message = exception.message, bot = tgBot))
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

    private fun processUnparsed(unparsedMessage: UnparsedMessage) {
        applicationEventPublisher.publishEvent(UnparsedMessageEvent(this, unparsedMessage))
    }

}