package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UnparsedMessage
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.getContextChatId
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service

@Service
class UnparsedMessagesCommandHandler(
    private val mongoTemplate: MongoTemplate,
    private val tgOperations: TgOperations,
    private val appConfig: AppConfig
) : CommandHandler, Authenticable {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun supportedBots(): Set<TgBot> = setOf(TgBot.NIKICHBOT)

    override suspend fun authenticate(update: Update): Boolean {
        if (update.getContextChatId() != appConfig.adminId) {
            tgOperations.replyToCurrentMessage("You are not allowed to use this bot ~_~")
            return false
        }
        return true
    }

    @HandleCommand("/unparsed")
    suspend fun listUnparsedMessages() {
        val unparsedMessages = mongoTemplate.findAll(UnparsedMessage::class.java)
        for (unparsedMessage in unparsedMessages) {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = unparsedMessage.toString()
            }
            delay(1_000)
        }
    }

    @HandleCommand("/reparse")
    suspend fun reparseUnparsedMessages() {
        val unparsedMessages = mongoTemplate.findAll(UnparsedMessage::class.java)
        log.info("Re-parsing started. ")
        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "Re-parsing started"
        }
        for (unparsedMessage in unparsedMessages) {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = "Re-parsing message: $unparsedMessage"
            }
            delay(1_000)
            mongoTemplate.remove(unparsedMessage)
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = "Message removed: $unparsedMessage"
            }
            delay(1_000)
        }
    }

}