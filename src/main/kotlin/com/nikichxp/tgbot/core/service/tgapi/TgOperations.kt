package com.nikichxp.tgbot.core.service.tgapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.TgBotConfig
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.service.helper.ErrorStorageService
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.core.util.getContextMessageId
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException.TooManyRequests
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity

@Service
class TgOperations(
    private val restTemplate: RestTemplate,
    private val tgSetWebhookService: TgBotSetWebhookService,
    private val tgUpdatePollService: TgUpdatePollService,
    private val tgBotConfig: TgBotConfig,
    private val appConfig: AppConfig,
    private val errorStorageService: ErrorStorageService,
    private val objectMapper: ObjectMapper,
) {

    private val bots = tgBotConfig.getInitializedBots()
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

    fun deleteWebhook(tgBot: TgBot) {
        restTemplate.getForEntity<String>(apiFor(tgBot) + "/deleteWebhook").body
    }

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        replyToMessageId: Long? = null,
        retryNumber: Int = 0,
    ) {
        sendMessage(chatId, text, replyToMessageId, retryNumber, getCurrentUpdateContext().tgBot)
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

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        replyToMessageId: Long? = null,
        retryNumber: Int = 0,
        tgBot: TgBot,
    ) {
        val args = mutableListOf<Pair<String, Any>>(
            "chat_id" to chatId,
            "text" to text
        )

        replyToMessageId?.apply { args += "reply_to_message_id" to replyToMessageId }

        try {
            restTemplate.postForEntity<String>("${apiFor(tgBot)}/sendMessage", args.toMap())
        } catch (tooManyRequests: TooManyRequests) {
            if (retryNumber <= 5) {
                logger.warn("429 error reached: try #$retryNumber, chatId = $chatId, text = $text")
                coroutineScope {
                    launch {
                        delay(5_000)
                        sendMessage(chatId, text, replyToMessageId, retryNumber + 1, tgBot)
                    }
                }
            } else {
                tooManyRequests.printStackTrace()
            }
        }
    }

    private suspend fun sendMessageInternal(
        message: TgSendMessage,
        tgBot: TgBot,
        retryNumber: Int = 0,
    ) {
        try {
            val body = objectMapper.valueToTree<JsonNode>(message)
            val response = restTemplate.postForEntity<TgSentMessageResponse>(
                "${apiFor(tgBot)}/sendMessage",
                request = body
            )
            message.callbacks.forEach {
                coroutineScope {
                    launch {
                        it(response.body!!)
                    }
                }
            }
        } catch (tooManyRequests: TooManyRequests) {
            if (retryNumber <= 5) {
                logger.warn("429 error reached: try #$retryNumber, message = $message")
                delay(5_000)
                return sendMessageInternal(message = message, tgBot = tgBot, retryNumber = retryNumber + 1)
            } else {
                throw tooManyRequests
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

        logger.info("Sending update message text: $body")

        val response = restTemplate.postForEntity<String>("${apiFor(bot)}/editMessageText", body)

        logger.info("Update message text: $response")
    }

    private fun apiFor(tgBot: TgBot): String {
        return "https://api.telegram.org/bot${tgBotConfig.getBotInfo(tgBot)!!.token}"
    }

}