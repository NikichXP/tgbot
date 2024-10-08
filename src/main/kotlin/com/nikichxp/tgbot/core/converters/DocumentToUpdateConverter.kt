package com.nikichxp.tgbot.core.converters

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.wnameless.json.flattener.JsonFlattener
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UnparsedMessage
import com.nikichxp.tgbot.core.util.diffWith
import org.bson.Document
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service

@Service
class DocumentToUpdateConverter(
    private val objectMapper: ObjectMapper,
    private val mongoTemplate: MongoTemplate
) : Converter<Document, Update> {

    override fun convert(body: Document): Update? {
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