package com.nikichxp.tgbot.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.wnameless.json.flattener.JsonFlattener
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.UnparsedMessage
import org.bson.Document
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.stream
import org.springframework.stereotype.Service

@Service
class MessageParser(
    private val objectMapper: ObjectMapper,
    private val mongoTemplate: MongoTemplate,
    private val messageClassifier: MessageClassifier
) {

    fun proceedRawData(body: Document) {
        val source = body.toJson()
        try {
            val update = objectMapper.readValue(source, Update::class.java)
            val control = objectMapper.writeValueAsString(update)

            val flatSrc = JsonFlattener.flattenAsMap(source)
            val flatCtr = JsonFlattener.flattenAsMap(control)

            if (source == control || flatSrc.keys == flatCtr.keys) {
                messageClassifier.proceedUpdate(update)
            } else {
                mongoTemplate.save(UnparsedMessage(body))
            }
        } catch (e: Exception) {
            mongoTemplate.save(UnparsedMessage(body))
        }
    }

}