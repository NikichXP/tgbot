package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.handlers.UpdateHandler
import com.nikichxp.tgbot.service.actions.LikedMessageService
import com.nikichxp.tgbot.util.getMarkers
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException

@Component
class UpdateRouter(
    private val handlers: List<UpdateHandler>,
    private val likedMessageService: LikedMessageService
) {

    private val handlerMarkers = handlers.associateBy { it.getMarkers() }

    fun proceedUpdate(update: Update) {
        val handlerList = getSupportedMarkerHandlers(update)
        if (handlerList.isEmpty()) {
            throw IllegalArgumentException("cant proceed message cause no handler for ${update.getMarkers()} found")
        }
        handlerList.forEach { it.handleUpdate(update) }
//        val result = handler.getResult(update) // then -> proceed results and give someone some karma

        // тут какой прикол, сейчас же я всё делаю чисто для бота с лайками
        // потому для MVP нужен только этот функционал
        // но потом я тут добавлю определение хендлеров и всё такое что бы делать разные штуки
        // СНАЧАЛА MVP

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


