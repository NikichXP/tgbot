package com.nikichxp.tgbot.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.error.ExpectedError
import com.nikichxp.tgbot.handlers.UpdateHandler
import com.nikichxp.tgbot.util.getMarkers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UpdateRouter(
    private val handlers: List<UpdateHandler>,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun proceedUpdate(update: Update) {
        val supportedHandlers = handlers.filter { isHandlerSupportedFor(update, it) }
        if (supportedHandlers.isEmpty()) {
            throw IllegalArgumentException("No handler found for ${objectMapper.writeValueAsString(update)}")
        }
        coroutineScope {
            val updateJobs = supportedHandlers.map { handler ->
                UpdateProcessContext(
                    update,
                    handler,
                    launch {
                        handler.handleUpdate(update)
                    })
            }
            updateJobs.forEach { waitForJob(it) }
        }
    }

    private suspend fun waitForJob(context: UpdateProcessContext) {
        try {
            context.job.join()
        } catch (expected: ExpectedError) {
            if (expected.printJson) {
                logger.warn(
                    "Handler ${context.handler::class.java.simpleName} didn't handled json: " +
                            objectMapper.writeValueAsString(context.update)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isHandlerSupportedFor(update: Update, handler: UpdateHandler): Boolean {
        val markerSupported = handler.getMarkers().all { update.getMarkers().contains(it) }
        val botSupported = handler.botSupported(update.bot)
        val handlerAllows = handler.canHandle(update)
        return markerSupported && botSupported && handlerAllows
    }

    data class UpdateProcessContext(
        val update: Update,
        val handler: UpdateHandler,
        val job: Job
    )

}
