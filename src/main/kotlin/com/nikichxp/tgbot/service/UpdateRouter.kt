package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.core.DuplicatedRatingError
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.handlers.UpdateHandler
import com.nikichxp.tgbot.util.getMarkers
import org.springframework.stereotype.Component

@Component
class UpdateRouter(
    private val handlers: List<UpdateHandler>
) {


    fun proceedUpdate(update: Update) {
        val handlerList = getSupportedMarkerHandlers(update)
        if (handlerList.isEmpty()) {
            throw IllegalArgumentException("cant proceed message cause no handler for ${update.getMarkers()} found")
        }
        handlerList.forEach {
            try {
                it.handleUpdate(update)
            } catch (dre: DuplicatedRatingError) {
                // ignore it
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

//@Component
//class PrivateMessageUpdateMarker : UpdateHandler {
//    override fun getMarkers(): Set<UpdateMarker> {
//        return setOf(MESSAGE_IN_CHAT, MESSAGE_WITH_TEXT)
//    }
//
//    override fun getResult(update: Update): MessageInteractionResult {
//        MessageInteractionResult.
//    }
//
//}


