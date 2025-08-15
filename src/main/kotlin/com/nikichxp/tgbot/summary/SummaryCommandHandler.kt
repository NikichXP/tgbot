package com.nikichxp.tgbot.summary

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.entity.bots.TgBot
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.service.tgapi.TgSendMessage
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.core.util.getMarkers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SummaryCommandHandler(
    private val tgOperations: TgOperations,
    private val summaryService: SummaryService
) : CommandHandler, UpdateHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun requiredFeatures() = setOf(Features.SUMMARY)
    override fun supportedBots() = setOf(TgBot.NIKICHBOT)

    override fun getMarkers() = setOf(UpdateMarker.MESSAGE_IN_GROUP)

    override suspend fun handleUpdate(update: Update) {
        val chatId = update.getContextChatId()
        if (chatId == null) {
            logger.warn("Cannot get chatId in update: $update")
            return
        }
        if (summaryService.getFeatureEnabledStatus(chatId)) {
            summaryService.saveUpdate(update)
        }
    }

    @HandleCommand("/summary")
    suspend fun processCommand(update: Update): Boolean {
        val chatId = update.getContextChatId() ?: throw IllegalArgumentException("Can't get chat id")
        if (!summaryService.getFeatureEnabledStatus(chatId)) {
            // ignore, don't let people know this feature exists so far
            return true
        }
        val message = TgSendMessage.create {
            replyToCurrentMessage()
            text = "Summary command is not implemented yet"
        }
        tgOperations.sendMessage(message, update.bot)
        return true
    }

    @HandleCommand("/summaryfeature")
    suspend fun toggleLogging(args: List<String>, update: Update): Boolean {
        if (!update.getMarkers().contains(UpdateMarker.MESSAGE_IN_GROUP)) {
            tgOperations.replyToCurrentMessage("This command is available only in group chats")
        }
        val chatId = update.getContextChatId() ?: throw IllegalArgumentException("Can't get chat id")

        val toggleStatus = args.first().toBooleanStrictOrNull()

        when (toggleStatus) {
            true -> summaryService.setFeatureEnabledStatus(chatId, true)
            false -> summaryService.setFeatureEnabledStatus(chatId, false)
            null -> {
                tgOperations.replyToCurrentMessage("Invalid argument. Use 'true' or 'false'")
                return false
            }
        }

        tgOperations.replyToCurrentMessage("Summary feature status is: ${summaryService.getFeatureEnabledStatus(chatId)}")
        return true
    }
}