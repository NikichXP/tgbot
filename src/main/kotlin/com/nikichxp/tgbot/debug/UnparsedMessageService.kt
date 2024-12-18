package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.entity.UnparsedMessageEvent
import kotlinx.coroutines.runBlocking
import org.springframework.context.ApplicationListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service

@Service
class UnparsedMessageService(
    private val sendMessageToAdminService: SendMessageToAdminService,
    private val mongoTemplate: MongoTemplate
) : ApplicationListener<UnparsedMessageEvent> {

    override fun onApplicationEvent(event: UnparsedMessageEvent) {
        val unparsedMessage = event.unparsedMessage
        mongoTemplate.save(unparsedMessage)
        runBlocking { // TODO fix that
            sendMessageToAdminService.sendMessage("New unparsed message (/unparsed)")
        }
    }

    // TODO reparse here as well
}