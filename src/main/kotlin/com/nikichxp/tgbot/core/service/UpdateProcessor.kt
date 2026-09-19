package com.nikichxp.tgbot.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.entity.StoredError
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.error.DisplayableError
import com.nikichxp.tgbot.core.error.ExpectedError
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
class UpdateProcessor(
    private val handlers: List<UpdateHandler>,
    private val objectMapper: ObjectMapper,
    private val tgMessageService: TgMessageService,
    private val mongoTemplate: MongoTemplate
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun proceedUpdate(updateContext: UpdateContext) {
        val supportedHandlers = handlers
            .filter { isHandlerSupportedForV2(updateContext, it) }
        if (supportedHandlers.isEmpty()) {
            throw IllegalArgumentException("No handler found for ${objectMapper.writeValueAsString(updateContext)}")
        }
        coroutineScope {
            val updateJobs = supportedHandlers.map { handler ->
                launch {
                    handleUpdate(handler, updateContext)
                }.let { job -> UpdateProcessContext(updateContext, handler, job) }
            }
            updateJobs.forEach { waitForJobCompletion(it) }
        }
    }

    private suspend fun handleUpdate(handler: UpdateHandler, updateContext: UpdateContext) {
        try {
            handler.handleUpdate(updateContext)
        } catch (displayableError: DisplayableError) {
            tgMessageService.sendMessage {
                replyToCurrentMessage()
                text = displayableError.displayedMessage
            }
        }
    }

    private suspend fun waitForJobCompletion(context: UpdateProcessContext) {
        try {
            context.job.join()
        } catch (expected: ExpectedError) {
            if (expected.printJson) {
                logger.warn(
                    "Handler ${context.handler::class.java.simpleName} didn't handled json: " +
                            objectMapper.writeValueAsString(context)
                )
            }
        } catch (e: Exception) {
            val message = "Handler ${context.handler::class.java.simpleName} failed to process update: " +
                    objectMapper.writeValueAsString(context.context)
            logger.error(message, e)
            mongoTemplate.save(StoredError(message, e.stackTraceToString()))
        }
    }

    private suspend fun isHandlerSupportedForV2(context: UpdateContext, handler: UpdateHandler): Boolean {
        val botInfo = context.getBotInfo()
        val markerSupported = context.markers.containsAll(handler.getMarkers())
        val botSupported = botInfo.getSupportedFeatures().containsAll(handler.requiredFeatures())
        val handlerAllows = handler.canHandle(context)
        val isAuthenticated = if (handler is Authenticable) {
            handler.authenticate(context)
        } else {
            true
        }
        return markerSupported && botSupported && handlerAllows && isAuthenticated
    }

    data class UpdateProcessContext(
        val context: UpdateContext,
        val handler: UpdateHandler,
        val job: Job
    )

}
