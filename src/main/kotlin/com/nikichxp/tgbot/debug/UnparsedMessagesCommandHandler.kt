package com.nikichxp.tgbot.debug

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UnparsedMessage
import com.nikichxp.tgbot.core.entity.bots.TgBot
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.MessageEntryPoint
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.getContextChatId
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.count
import org.springframework.data.mongodb.core.findAll
import org.springframework.stereotype.Service

@Service
class UnparsedMessagesCommandHandler(
    private val mongoTemplate: MongoTemplate,
    private val tgOperations: TgOperations,
    private val appConfig: AppConfig,
    private val objectMapper: ObjectMapper,
    @Lazy private val messageEntryPoint: MessageEntryPoint
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
        val unparsedMessages = mongoTemplate.findAll<UnparsedMessage>()

        if (unparsedMessages.isEmpty()) {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = "No unparsed messages"
            }
            return
        }

        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "Unparsed messages: ${unparsedMessages.size}"
        }

        for (unparsedMessage in unparsedMessages.shuffled().take(10)) {
            delay(500)
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(unparsedMessage.content)
            }
        }
    }

    @HandleCommand("/reparse")
    suspend fun reparseUnparsedMessages() {
        val unparsedMessages = mongoTemplate.findAll<UnparsedMessage>()
        log.info("Re-parsing started. Task queue: ${unparsedMessages.size}")
        for (unparsedMessage in unparsedMessages) {
            mongoTemplate.remove(unparsedMessage)
            messageEntryPoint.proceedRawData(unparsedMessage.content, unparsedMessage.bot)
        }
        val result = mongoTemplate.count<UnparsedMessage>()
        log.info("Re-parsing finished. Unparsed messages left: $result")
        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "Re-parsing finished. Unparsed messages left: $result/${unparsedMessages.size}"
        }
    }

}