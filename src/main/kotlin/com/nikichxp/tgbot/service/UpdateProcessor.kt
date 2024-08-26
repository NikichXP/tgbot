package com.nikichxp.tgbot.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.UpdateContext
import com.nikichxp.tgbot.error.ExpectedError
import com.nikichxp.tgbot.handlers.UpdateHandler
import com.nikichxp.tgbot.util.getMarkers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UpdateProcessor(
    private val handlers: List<UpdateHandler>,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun proceedUpdate(update: UpdateContext) {
        val supportedHandlers = handlers.filter { isHandlerSupportedFor(update, it) }
        if (supportedHandlers.isEmpty()) {
            throw IllegalArgumentException("No handler found for ${objectMapper.writeValueAsString(update)}")
        }
        coroutineScope {
            val updateJobs = supportedHandlers.map { handler ->
                launch {
                    handler.handleUpdate(update)
                }.let { job -> UpdateProcessContext(update, handler, job) }
            }
            updateJobs.forEach { waitForJobCompletion(it) }
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
            e.printStackTrace()
        }
    }

    private fun isHandlerSupportedFor(context: UpdateContext, handler: UpdateHandler): Boolean {
        val markerSupported = handler.getMarkers().all { context.update.getMarkers().contains(it) }
        val botSupported = handler.botSupported(context.tgBot)
        val handlerAllows = handler.canHandle(context)
        return markerSupported && botSupported && handlerAllows
    }

    data class UpdateProcessContext(
        val context: UpdateContext,
        val handler: UpdateHandler,
        val job: Job
    )

}
