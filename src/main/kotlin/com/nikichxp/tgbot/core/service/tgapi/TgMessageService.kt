package com.nikichxp.tgbot.core.service.tgapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.error.TgApiCallException
import com.nikichxp.tgbot.core.service.helper.ErrorService
import com.nikichxp.tgbot.core.service.tgapi.executor.ITgApiCallExecutor
import com.nikichxp.tgbot.core.service.tgapi.executor.TgMultipartPart
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.core.util.getContextMessageId
import com.nikichxp.tgbot.core.util.getCurrentUpdateContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// TODO make message service non-bound to telegram, it should create a message-to-send entity and route it
//  to correct method executor
@Service
class TgMessageService(
    private val tgApiCallExecutor: ITgApiCallExecutor,
    private val errorService: ErrorService,
    private val objectMapper: ObjectMapper
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
        val tgBotInfo = getCurrentUpdateContext().getBotInfo() as? TgBotInfo
            ?: throw IllegalArgumentException("TgBotInfo is not an instance of TgBotInfo")
        sendMessage(tgBotInfo, messageDSL)
    }

    suspend fun sendMessage(tgBot: TgBotInfo, messageDSL: suspend TgSendMessage.() -> Unit) {
        val message = TgSendMessage.create(messageDSL)
        sendMessage(message, tgBot)
    }

    suspend fun sendMessage(
        message: TgSendMessage,
        tgBot: TgBotInfo,
    ) {
        val rawResponse = tgApiCallExecutor.callEndpoint(tgBot, "sendMessage", message)
        val response = objectMapper.treeToValue(rawResponse.content, TgSentMessageResponse::class.java)
        message.callbacks.forEach { it(response) }
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

    suspend fun editMessageText(
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

        val response = tgApiCallExecutor.callEndpoint(bot, "editMessageText", body)

        logger.info("Edit message text: ${response.content}")
    }

    suspend fun editMessageText(
        chatId: Long,
        messageId: Long,
        text: String,
        replyMarkup: TgReplyMarkup? = null,
    ) {
        val tgBotInfo = getCurrentUpdateContext().getBotInfo() as? TgBotInfo
            ?: throw IllegalArgumentException("TgBotInfo is not an instance of TgBotInfo")
        editMessageText(chatId, messageId, text, tgBotInfo, replyMarkup)
    }

    suspend fun editMessageText(
        text: String,
        replyMarkup: TgReplyMarkup? = null,
    ) {
        val update = getCurrentUpdate()
        val chatId = update.getContextChatId()
        val messageId = update.getContextMessageId()
        if (chatId != null && messageId != null) {
            val tgBotInfo = getCurrentUpdateContext().getBotInfo() as? TgBotInfo
                ?: throw IllegalArgumentException("TgBotInfo is not an instance of TgBotInfo")
            editMessageText(chatId, messageId, text, tgBotInfo, replyMarkup)
        } else {
            errorService.logAndReportError(
                logger,
                "Cannot edit message text in current context: $text",
                update
            )
        }
    }

    suspend fun sendDocument(
        chatId: Long,
        bot: TgBotInfo,
        fileName: String,
        fileContent: ByteArray,
        caption: String? = null,
        replyToMessageId: Long? = null
    ) {
        val parts = mutableListOf<TgMultipartPart>(
            TgMultipartPart.Text("chat_id", chatId.toString())
        )
        caption?.let { parts.add(TgMultipartPart.Text("caption", it)) }
        replyToMessageId?.let {
            parts.add(TgMultipartPart.Text("reply_parameters", """{"message_id":$it,"chat_id":$chatId}"""))
        }
        parts.add(TgMultipartPart.FilePart("document", fileName, "text/markdown", fileContent))

        val response = tgApiCallExecutor.callEndpointMultipart(bot, "sendDocument", parts)

        if (!response.success) {
            logger.warn("sendDocument failed: chatId={}, body={}", chatId, response.content)
            throw TgApiCallException("sendDocument failed: ${response.content}")
        }

        logger.info("Sent document {} to chatId={}", fileName, chatId)
    }
}