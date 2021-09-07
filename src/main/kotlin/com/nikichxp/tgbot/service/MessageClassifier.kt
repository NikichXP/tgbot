package com.nikichxp.tgbot.service

import com.github.wnameless.json.flattener.JsonFlattener
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.MessageInteractionResult
import com.nikichxp.tgbot.util.listUpdated
import org.bson.Document
import org.springframework.stereotype.Component

@Component
class MessageClassifier(
    private val handlers: List<UpdateHandler>
) {

//    fun getMessageHandler(document: Document) {
//        val data = JsonFlattener(document.toJson()).flattenAsMap()
//        val supportedHandlers = handlers.filter { it.canHandle(document, data) }
//        if (supportedHandlers.size == 1) {
//            supportedHandlers.first().getResult(document)
//        } else {
//            println("compatible handler count != 1: " + supportedHandlers.map { it::class.java.name })
//        }
//        println()
//    }

    fun proceedUpdate(update: Update) {
        val supportedHandlers = handlers.filter { it.canHandle(update) }
        if (supportedHandlers.size == 1) {
            supportedHandlers.first().getResult(update)
        } else {
            println("compatible handler count != 1: " + supportedHandlers.map { it::class.java.name })
        }
    }

}

// TODO weight of the handler - default is 0, most precise = 10000
interface UpdateHandler {
    fun canHandle(update: Update): Boolean
    fun getResult(update: Update): MessageInteractionResult
}

@Component
class TextUpdateHandler : UpdateHandler {
    override fun canHandle(update: Update): Boolean {
        val updated = update.listUpdated()

        return true
    }

    override fun getResult(update: Update): MessageInteractionResult {
        return MessageInteractionResult("")
    }
}

