package com.nikichxp.tgbot.summary

import com.nikichxp.tgbot.core.auth.TrustedUserService
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.entity.TgUpdateContext
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.error.ConfigMapViolationException
import com.nikichxp.tgbot.core.error.DisplayableError
import com.nikichxp.tgbot.core.error.PermissionDeniedError
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import com.nikichxp.tgbot.core.util.ChatCommandParser
import com.nikichxp.tgbot.summary.entity.RecapOptions
import com.nikichxp.tgbot.summary.entity.RecapOptionsBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun requiredFeatures() = setOf(Features.SUMMARY)

    override fun getMarkers() = setOf(UpdateMarker.MESSAGE_IN_GROUP)

    override suspend fun handleUpdate(updateContext: UpdateContext) {
        val chatId = updateContext.chat?.id ?: run {
            logger.warn("Cannot get chatId in update: $updateContext")
            return
        }

        if (summaryService.getFeatureEnabledStatus(chatId)) {
            summaryMessageStorageService.storeMessage(updateContext)
        }
    }

    @HandleCommand("/whatsup")
    suspend fun whatsup(args: List<String>, updateContext: UpdateContext): Boolean {
        val chatId = updateContext.chat?.id ?: throw IllegalArgumentException("Can't get chat id")

        if (!summaryService.getFeatureEnabledStatus(chatId)) {
            // ignore, don't let people know this feature exists so far
            return true
        }

        val options = getRecapOptions(args, chatId, updateContext)
        tgMessageService.sendMessage {
            replyToCurrentMessage()
            text = "Генерирую сводку... ${options.getExtraOptionsString()}"
        }

        CoroutineScope(Dispatchers.IO + updateContext as TgUpdateContext).launch {
            try {
                val recap = summaryService.getRecap(options)

                logger.info("Recap generated: $recap")

                val textToDisplay = recap.recap

                tgMessageService.sendMessage {
                    replyToCurrentMessage()
                    text = textToDisplay
                }
            } catch (e: Exception) {
                e.printStackTrace()
                tgMessageService.sendMessage {
                    replyToCurrentMessage()
                    text = "Произошла ошибка при генерации сводки (${e.javaClass.simpleName})"
                }
            }
        }

        return true
    }

    @HandleCommand("/summaryfeature")
    suspend fun toggleLogging(args: List<String>, updateContext: UpdateContext): Boolean {
        checkAccess(updateContext)

        val chatId = updateContext.getChatId()
        val toggleStatus = args.first().toBooleanStrictOrNull()

        when (toggleStatus) {
            true -> summaryService.setFeatureEnabledStatus(chatId, true)
            false -> summaryService.setFeatureEnabledStatus(chatId, false)
            null -> throw DisplayableError("Invalid argument. Use 'true' or 'false'")
        }

        tgMessageService.replyToCurrentMessage(
            "Summary feature status is: ${summaryService.getFeatureEnabledStatus(chatId)}"
        )
        return true
    }

    @HandleCommand("/recap-default-model")
    suspend fun setDefaultRecapModel(args: List<String>, updateContext: UpdateContext): Boolean {
        checkAccess(updateContext)
        val modelName = args.first()

        summaryService.setDefaultModel(modelName)

        return true
    }

    private suspend fun checkAccess(updateContext: UpdateContext) {
        if (!updateContext.markers.contains(UpdateMarker.MESSAGE_IN_GROUP)) {
            tgMessageService.replyToCurrentMessage("This command is available only in group chats")
        }

        val callerId = updateContext.from?.id ?: throw IllegalArgumentException("Can't get userId")

        if (callerId != appConfig.adminId) {
            tgMessageService.sendMessage {
                replyToCurrentMessage()
                text = "You are not allowed to use this command"
            }
            throw PermissionDeniedError("You are not allowed to use this command")
        }
    }

    private suspend fun getRecapOptions(args: List<String>, chatId: Long, updateContext: UpdateContext): RecapOptions {

        val recapOptionsBuilder = RecapOptionsBuilder()

        if (args.size > 1) {
            ChatCommandParser.analyze(args) {
                path("days") {
                    asArg("days") {
                        vars["days"]?.toIntOrNull()?.let { recapOptionsBuilder.days = it }
                    }
                }
                path("model") {
                    asArg("modelName") {
                        if (!trustedUserService.isTrusted(updateContext)) {
                            tgMessageService.replyToCurrentMessage("Выбор модели вам недоступен")
                        } else {
                            recapOptionsBuilder.model = vars["modelName"] ?: throw ConfigMapViolationException()
                        }
                    }
                }
                path("since") {
                    asArg("since") {
                        vars["since"]?.let { recapOptionsBuilder.since = SummaryDateUtil.parseSince(it) }
                    }
                }
            }
        }

        return recapOptionsBuilder.build(chatId)
    }

}
