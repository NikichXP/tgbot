package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.tooling.RawJsonLogger
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service

@Service
class MessageEntryPoint(
    private val conversionService: ConversionService,
    private val updateRouter: UpdateRouter,
    private val rawJsonLogger: RawJsonLogger
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun proceedRawData(body: Document, bot: TgBot) {
        logger.info("Received message: $body")
        rawJsonLogger.logEvent(body)
        val update = conversionService.convert(body, Update::class.java)
            ?: throw IllegalArgumentException("Cannot convert the message")
        proceedUpdate(update, bot)
    }

    suspend fun proceedUpdate(update: Update, bot: TgBot) {
        update.bot = bot
        updateRouter.proceedUpdate(update)
    }

}