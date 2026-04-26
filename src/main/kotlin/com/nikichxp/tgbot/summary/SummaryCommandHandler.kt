package com.nikichxp.tgbot.summary

import com.nikichxp.tgbot.core.auth.TrustedUserService
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import com.nikichxp.tgbot.core.service.tgapi.TgSendMessage
import com.nikichxp.tgbot.core.util.ChatCommandParser
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.core.util.getContextUserId
import com.nikichxp.tgbot.core.util.getMarkers
import com.nikichxp.tgbot.summary.entity.RecapOptions
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SummaryCommandHandler(
    private val tgMessageService: TgMessageService,
    private val summaryService: SummaryService,
    private val summaryMessageStorageService: SummaryMessageStorageService,
    private val appConfig: AppConfig,
    private val trustedUserService: TrustedUserService
) : CommandHandler, UpdateHandler {

    private val defaultModel = "google/gemma-4-31b-it"

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun requiredFeatures() = setOf(Features.SUMMARY)

    override fun getMarkers() = setOf(UpdateMarker.MESSAGE_IN_GROUP)

    override suspend fun handleUpdate(update: Update) {
        val chatId = update.getContextChatId() ?: run {
            logger.warn("Cannot get chatId in update: $update")
            return
        }

        if (summaryService.getFeatureEnabledStatus(chatId)) {
            summaryMessageStorageService.storeMessage(update)
        }
    }

    @HandleCommand("/enable_summary")
    suspend fun enableSummary(update: Update) {
        val chatId = update.getContextChatId() ?: throw IllegalArgumentException("Can't get chat id")
        val callerId = update.getContextUserId() ?: throw IllegalArgumentException("Can't get userId")

        if (callerId != appConfig.adminId) {
            tgMessageService.sendMessage {
                replyToCurrentMessage()
                text = "You are not allowed to use this command"
            }
            return
        }

        summaryService.setFeatureEnabledStatus(chatId, true)
        tgMessageService.sendMessage {
            replyToCurrentMessage()
            text = "Summary enabled"
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
            text = "Эта фича ещё не готова!"
        }
        tgMessageService.sendMessage(message, update.bot)
        return true
    }

    @HandleCommand("/whatsup")
    suspend fun whatsup(args: List<String>, update: Update): Boolean {
        val chatId = update.getContextChatId() ?: throw IllegalArgumentException("Can't get chat id")

        if (!summaryService.getFeatureEnabledStatus(chatId)) {
            // ignore, don't let people know this feature exists so far
            return true
        }

        var modelName: String? = defaultModel
        var days: Long = 1

        if (args.size > 1) {
            ChatCommandParser.analyze(args) {
                path("model") {
                    asArg("modelName") {
                        if (!trustedUserService.isTrusted(update)) {
                            tgMessageService.replyToCurrentMessage("Выбор модели вам недоступен")
                        } else {
                            modelName = vars["modelName"]
                        }
                    }
                }
                path("days") {
                    asArg("days") {
                        vars["days"]?.toLongOrNull()?.let { days = it }
                    }
                }
            }
        }

        tgMessageService.sendMessage {
            replyToCurrentMessage()
            text = "Вы почти у цели!"
        }

        val options = RecapOptions(
            chatId = chatId,
            days = days,
            model = modelName
        )

        val recap = summaryService.getRecap(options)

        tgMessageService.sendMessage {
            replyToCurrentMessage()
            text = recap
        }

        return true
    }

    @HandleCommand("/summaryfeature")
    suspend fun toggleLogging(args: List<String>, update: Update): Boolean {
        if (!update.getMarkers().contains(UpdateMarker.MESSAGE_IN_GROUP)) {
            tgMessageService.replyToCurrentMessage("This command is available only in group chats")
        }
        val chatId = update.getContextChatId() ?: throw IllegalArgumentException("Can't get chat id")

        val toggleStatus = args.first().toBooleanStrictOrNull()

        when (toggleStatus) {
            true -> summaryService.setFeatureEnabledStatus(chatId, true)
            false -> summaryService.setFeatureEnabledStatus(chatId, false)
            null -> {
                tgMessageService.replyToCurrentMessage("Invalid argument. Use 'true' or 'false'")
                return false
            }
        }

        tgMessageService.replyToCurrentMessage(
            "Summary feature status is: ${
                summaryService.getFeatureEnabledStatus(
                    chatId
                )
            }"
        )
        return true
    }
}