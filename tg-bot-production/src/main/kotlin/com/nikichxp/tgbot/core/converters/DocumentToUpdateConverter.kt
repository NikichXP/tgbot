package com.nikichxp.tgbot.core.converters

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgUpdateFieldsEvent
import com.nikichxp.tgbot.core.entity.UnparsedMessage
import com.nikichxp.tgbot.core.entity.UnparsedMessageEvent
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.util.JsonFlattenerService
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class DocumentToUpdateConverter(
    private val objectMapper: ObjectMapper,
    private val jsonFlattenerService: JsonFlattenerService,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun convert(body: Document, tgBot: TgBotInfo): Update? {
        val source = body.toJson()
        trackUpdateFields(source, tgBot)
        try {
            return objectMapper.readValue(source, Update::class.java)
        } catch (exception: Exception) {
            exception.printStackTrace()
            processUnparsed(UnparsedMessage(body, message = exception.message, bot = tgBot))
        }
        throw IllegalArgumentException("Cannot convert the incoming message")
    }

    private fun trackUpdateFields(source: String, tgBot: TgBotInfo) {
        try {
            val paths = jsonFlattenerService.extractLeafPaths(source)
            applicationEventPublisher.publishEvent(TgUpdateFieldsEvent(this, paths, tgBot))
        } catch (e: Exception) {
            log.warn("Failed to extract update fields", e)
        }
    }

    private fun processUnparsed(unparsedMessage: UnparsedMessage) {
        applicationEventPublisher.publishEvent(UnparsedMessageEvent(this, unparsedMessage))
    }

}