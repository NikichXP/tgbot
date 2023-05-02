package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.error.ExpectedError
import com.nikichxp.tgbot.handlers.UpdateHandler
import com.nikichxp.tgbot.util.getMarkers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UpdateRouter(
    private val handlers: List<UpdateHandler>
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun proceedUpdate(update: Update) {
        val handlerList = getSupportedMarkerHandlers(update)
        if (handlerList.isEmpty()) {
            throw IllegalArgumentException("cant proceed message cause no handler for ${update.getMarkers()} found")
        }
        handlerList.forEach {
            try {
                it.handleUpdate(update)
            } catch (expected: ExpectedError) {
                if (expected.printJson) {
                    logger.warn("Handler ${it::class.java.simpleName} didn't handled json: ${update.toJson()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getSupportedMarkerHandlers(update: Update): List<UpdateHandler> {
        val required = update.getMarkers()
        return handlers.filter { required.containsAll(it.getMarkers()) }
    }

}
