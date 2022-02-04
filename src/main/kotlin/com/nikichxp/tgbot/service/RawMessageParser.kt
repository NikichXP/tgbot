package com.nikichxp.tgbot.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.wnameless.json.flattener.JsonFlattener
import com.nikichxp.tgbot.core.CurrentUpdateProvider
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.UnparsedMessage
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service

@Service
class RawMessageParser(
    private val objectMapper: ObjectMapper,
    private val mongoTemplate: MongoTemplate,
    private val messageClassifier: MessageClassifier,
    private val currentUpdateProvider: CurrentUpdateProvider
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun proceedRawData(body: Document) {
        val source = body.toJson()
        logger.info("---- Processing JSON: $source")
        try {
            val update = objectMapper.readValue(source, Update::class.java)
            val control = objectMapper.writeValueAsString(update)

            val flatSrc = JsonFlattener.flattenAsMap(source)
            val flatCtr = JsonFlattener.flattenAsMap(control)

            if (source == control || flatSrc.keys == flatCtr.keys) {
                currentUpdateProvider.update = update
                messageClassifier.proceedUpdate(update)
            } else {
                mongoTemplate.save(UnparsedMessage(body))
            }
        } catch (e: Exception) {
            mongoTemplate.save(UnparsedMessage(body))
        }
    }

}