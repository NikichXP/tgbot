package com.nikichxp.tgbot.core.service.tgapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.service.helper.ErrorStorageService
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.core.util.getContextMessageId
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TgOperations(
    private val tgSetWebhookService: TgBotSetWebhookService,
    private val tgUpdatePollService: TgUpdatePollService,
    private val tgMethodExecutor: TgMethodExecutor,
    private val appConfig: AppConfig,
    private val errorStorageService: ErrorStorageService,
    private val objectMapper: ObjectMapper,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private suspend fun getCurrentUpdateContext(): UpdateContext = coroutineScope {
        this.coroutineContext[UpdateContext] ?: try {
            throw IllegalStateException("No update context found")
        } catch (e: Exception) {
            errorStorageService.logAndReportError(logger, "No update context found", e)
            throw e
        }
    }

    private suspend fun getCurrentUpdate(): Update = getCurrentUpdateContext().update

    suspend fun deleteWebhook(tgBot: TgBot) {
        tgMethodExecutor.execute(tgBot, "deleteWebhook", mapOf<String, Any>())
    }

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        replyToMessageId: Long? = null,
        retryNumber: Int = 0,
    ) {
        sendMessage {
            this.chatId = chatId
            this.text = text
            this.replyParameters = replyToMessageId?.let { TgReplyParameters(chatId, it) }
        }
    }

    suspend fun sendMessage(messageDSL: suspend TgSendMessage.() -> Unit) {
        sendMessage(getCurrentUpdateContext().tgBot, messageDSL)
    }

    suspend fun sendMessage(tgBot: TgBot, messageDSL: suspend TgSendMessage.() -> Unit) {
        val message = TgSendMessage.create(messageDSL)
        sendMessageInternal(message, tgBot)
    }

    suspend fun sendMessage(
        message: TgSendMessage,
        tgBot: TgBot,
    ) {
        sendMessageInternal(message, tgBot)
    }

    private suspend fun sendMessageInternal(
        message: TgSendMessage,
        tgBot: TgBot,
        retryNumber: Int = 0,
    ) {
        val rawResponse = tgMethodExecutor.execute(tgBot, "sendMessage", message)
        val response = objectMapper.treeToValue(rawResponse.body, TgSentMessageResponse::class.java)
        message.callbacks.forEach {
            coroutineScope {
                launch {
                    it(response)
                }
            }
        }
    }

    suspend fun sendToCurrentChat(text: String, replyMarkup: TgReplyMarkup? = null) {
        val update = getCurrentUpdate()
        update.getContextChatId()?.let {
            sendMessage(it, text)
        } ?: errorStorageService.logAndReportError(logger, "Cannot send message reply to current chat: $text", update)
    }

    suspend fun replyToCurrentMessage(text: String, replyMarkup: TgReplyMarkup? = null) {

        val update = getCurrentUpdate()
        update.getContextChatId()?.let {
            sendMessage(it, text, update.getContextMessageId())
        } ?: errorStorageService.logAndReportError(
            logger,
            "Cannot send message reply to current message: $text",
            update
        )
    }

    suspend fun updateMessageText(
        chatId: Long,
        messageId: Long,
        text: String,
        bot: TgBot,
        replyMarkup: TgReplyMarkup? = null,
    ) {

        val args = mutableMapOf<String, Any>(
            "chat_id" to chatId,
            "message_id" to messageId,
            "text" to text
        )

        replyMarkup?.let { args["reply_markup"] = replyMarkup }

        val body = objectMapper.valueToTree<JsonNode>(args)

        val response = tgMethodExecutor.execute(bot, "editMessageText", body)

        logger.info("Update message text: ${response.body}")
    }
}