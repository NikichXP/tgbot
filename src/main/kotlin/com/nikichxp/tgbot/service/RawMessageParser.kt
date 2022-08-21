package com.nikichxp.tgbot.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.github.wnameless.json.flattener.JsonFlattener
import com.nikichxp.tgbot.core.CurrentUpdateProvider
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.UnparsedMessage
import com.nikichxp.tgbot.tooling.RawJsonLogger
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

    fun proceedRawData(body: Document) {
        rawJsonLogger.logEvent(body)
        val source = body.toJson()
        try {
            val update = objectMapper.readValue(source, Update::class.java)
            val control = objectMapper.writeValueAsString(update)

            val flatSrc = JsonFlattener.flattenAsMap(source)
            val flatCtr = JsonFlattener.flattenAsMap(control)

            if (source == control || flatSrc.keys == flatCtr.keys) {
                currentUpdateProvider.update = update
                updateRouter.proceedUpdate(update)
            } else {
                mongoTemplate.save(UnparsedMessage(body, missedKeys = flatSrc.keys - flatCtr.keys))
            }
        } catch (parseException: UnrecognizedPropertyException) {
            logger.warn("detected unparsed message, see db for more info")
            mongoTemplate.save(UnparsedMessage(body, message = parseException.message))
        } catch (e: Exception) {
            e.printStackTrace()
            mongoTemplate.save(UnparsedMessage(body))
        }
    }

}