package com.nikichxp.tgbot.service

import com.github.wnameless.json.flattener.JsonFlattener
import com.nikichxp.tgbot.entity.MessageInteractionResult
import org.bson.Document
import org.springframework.stereotype.Component

@Component
class MessageClassifier(
    private val handlers: List<MessageHandler>
) {

    fun getMessageHandler(document: Document) {
        val data = JsonFlattener(document.toJson()).flattenAsMap()
        val supportedHandlers = handlers.filter { it.canHandle(document, data) }
        if (supportedHandlers.size == 1) {
            supportedHandlers.first().getResult(document)
        } else {
            println("compatible handler count != 1: " + supportedHandlers.map { it::class.java.name })
        }
        println()
    }

}

interface MessageHandler {
    fun canHandle(document: Document, data: MutableMap<String, Any>): Boolean
    fun getResult(document: Document): MessageInteractionResult

    fun getChatId(document: Document, data: MutableMap<String, Any>): Long {
        return data["message.chat.id"]?.let {
            return@let when (it) {
                is Int -> it.toLong()
                is Long -> it
                else -> 0
            }
        } ?: 0
    }
}

@Component
class TextMessageHandler : MessageHandler {
    override fun canHandle(document: Document, data: MutableMap<String, Any>): Boolean {
        val id = getChatId(document, data)

        if (id >= 0) {
            return false
        }

        return true
    }

    override fun getResult(document: Document): MessageInteractionResult {
        return MessageInteractionResult("")
    }
}

