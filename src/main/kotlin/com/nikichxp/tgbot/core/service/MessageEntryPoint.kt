package com.nikichxp.tgbot.core.service

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.tooling.RawJsonLogger
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service

@Service
class MessageEntryPoint(
    private val conversionService: ConversionService,
    private val updateProcessor: UpdateProcessor,
    private val rawJsonLogger: RawJsonLogger
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun proceedRawData(body: Document, bot: TgBot) {
        logger.info("Received message: $body")
        rawJsonLogger.logEvent(body)
        try {
            val update = conversionService.convert(body, Update::class.java)
                ?: throw IllegalArgumentException("Cannot convert the message")
            proceedUpdate(update, bot)
        } catch (e: Exception) {
            logger.error("Failed to process the message", e)
        }
    }

    suspend fun proceedUpdate(update: Update, bot: TgBot) {
        update.bot = bot
        val updateContext = UpdateContext(update, bot)
        coroutineScope {
            withContext(this.coroutineContext + updateContext) {
                updateProcessor.proceedUpdate(updateContext)
            }
        }
    }

}