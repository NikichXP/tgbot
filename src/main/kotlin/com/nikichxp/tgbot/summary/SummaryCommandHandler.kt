package com.nikichxp.tgbot.summary

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.service.tgapi.TgSendMessage
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.core.util.getMarkers
import org.springframework.stereotype.Service

@Service
class SummaryCommandHandler(
    private val tgOperations: TgOperations
) : CommandHandler {

    override fun supportedBots(tgBot: TgBot) = setOf(TgBot.NIKICHBOT)

    @HandleCommand("/summary")
    suspend fun processCommand(update: Update): Boolean {
        val chatId = update.getContextChatId() ?: throw IllegalArgumentException("Can't get chat id")
        val message = TgSendMessage(
            chatId = chatId,
            text = "Summary command is not implemented yet"
        )
        tgOperations.sendMessage(message, update.bot)
        return true
    }

    @HandleCommand("/summaryfeature")
    suspend fun toggleLogging(args: List<String>, update: Update): Boolean {
        if (!update.getMarkers().contains(UpdateMarker.MESSAGE_IN_GROUP)) {
            tgOperations.replyToCurrentMessage("This command is available only in group chats")
        }
        val chatId = update.getContextChatId() ?: throw IllegalArgumentException("Can't get chat id")
        val message = TgSendMessage(
            chatId = chatId,
            text = "Summary feature is not implemented yet"
        )
        tgOperations.sendMessage(message, update.bot)
        return true
    }
}