package com.nikichxp.tgbot.core.service.tgapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.bots.BotInfo
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.service.TgBotV2Service
import com.nikichxp.tgbot.core.service.helper.ErrorService
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.core.util.getContextMessageId
import com.nikichxp.tgbot.core.util.getCurrentUpdateContext
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// TODO make message service non-bound to telegram, it should create a message-to-send entity and route it
//  to correct method executor
@Service
class TgMessageService(
    private val tgMethodExecutor: TgMethodExecutor,
    private val errorService: ErrorService,
    private val objectMapper: ObjectMapper,
    private val tgBotService: TgBotV2Service,
    private val httpClient: HttpClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private suspend fun getCurrentUpdate(): Update = getCurrentUpdateContext().getUpdate()

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        replyToMessageId: Long? = null
    ) {
        sendMessage {
            this.chatId = chatId
            this.text = text
            this.replyParameters = replyToMessageId?.let { TgReplyParameters(chatId, it) }
        }
    }

    suspend fun sendMessage(messageDSL: suspend TgSendMessage.() -> Unit) {
        sendMessage(getCurrentUpdateContext().getBotInfo(), messageDSL)
    }

    suspend fun sendMessage(tgBot: BotInfo, messageDSL: suspend TgSendMessage.() -> Unit) {
        val message = TgSendMessage.create(messageDSL)
        sendMessage(message, tgBot)
    }

    suspend fun sendMessage(
        message: TgSendMessage,
        tgBot: BotInfo,
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

    suspend fun replyToCurrentMessage(text: String, replyMarkup: TgReplyMarkup? = null) {

        val update = getCurrentUpdate()
        update.getContextChatId()?.let {
            sendMessage(it, text, update.getContextMessageId())
        } ?: errorService.logAndReportError(
            logger,
            "Cannot send message reply to current message: $text",
            update
        )
    }

    suspend fun updateMessageText(
        chatId: Long,
        messageId: Long,
        text: String,
        bot: TgBotInfo,
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

    suspend fun sendDocument(
        chatId: Long,
        bot: TgBotInfo,
        fileName: String,
        fileContent: ByteArray,
        caption: String? = null,
        replyToMessageId: Long? = null
    ) {
        val token = tgBotService.getTokenById(bot.name)
        val url = "https://api.telegram.org/bot$token/sendDocument"

        val response = httpClient.post(url) {
            header(HttpHeaders.Accept, "application/json")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("chat_id", chatId.toString())
                        if (caption != null) append("caption", caption)
                        if (replyToMessageId != null) {
                            append("reply_parameters", """{"message_id":$replyToMessageId,"chat_id":$chatId}""")
                        }
                        append(
                            key = "document",
                            value = fileContent,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, "text/markdown")
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            }
                        )
                    }
                )
            )
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.bodyAsText() }.getOrDefault("")
            logger.warn("sendDocument failed: chatId={}, status={}, body={}", chatId, response.status, body)
            error("sendDocument failed with status ${response.status}: $body")
        }

        logger.info("Sent document {} to chatId={}", fileName, chatId)
    }
}