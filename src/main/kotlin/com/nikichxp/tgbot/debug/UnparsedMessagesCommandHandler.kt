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
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service

@Service
class UnparsedMessagesCommandHandler(
    private val mongoTemplate: MongoTemplate,
    private val tgOperations: TgOperations,
    private val appConfig: AppConfig
) : CommandHandler, Authenticable {

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
        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = unparsedMessages.joinToString("\n") { it.toString() }
        }
    }

}