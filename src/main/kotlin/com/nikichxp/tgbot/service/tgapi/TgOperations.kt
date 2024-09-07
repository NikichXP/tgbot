package com.nikichxp.tgbot.service.tgapi

import com.nikichxp.tgbot.config.AppConfig
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.entity.TgBotConfig
import com.nikichxp.tgbot.entity.UpdateContext
import com.nikichxp.tgbot.service.helper.ErrorStorageService
import com.nikichxp.tgbot.util.getContextChatId
import com.nikichxp.tgbot.util.getContextMessageId
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
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
    private val errorStorageService: ErrorStorageService
) {

    private val bots = tgBotConfig.getInitializedBots()
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun registerWebhooks() {
        if (appConfig.localEnv || appConfig.suspendBotRegistering) {
            logger.info("Local env: skip webhook setting")
            bots.forEach { tgUpdatePollService.startPollingFor(it) }
        } else {
            logger.info("Registering bots: ${bots.map { it.bot }}")
            bots.forEach {
                val webhookSet = runBlocking { tgSetWebhookService.register(it) }
                if (!webhookSet) {
                    logger.warn("Webhook setting failed for bot: ${it.bot}, doing polling instead")
                    tgUpdatePollService.startPollingFor(it)
                }
            }
        }
    }

    suspend fun apiForCurrentBot() = apiFor(getCurrentUpdateContext().tgBot)

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
        retryNumber: Int = 0
    ) {
        val args = mutableListOf<Pair<String, Any>>(
            "chat_id" to chatId,
            "text" to text
        )

        replyToMessageId?.apply { args += "reply_to_message_id" to replyToMessageId }

        try {
            restTemplate.postForEntity<String>("${apiForCurrentBot()}/sendMessage", args.toMap())
        } catch (tooManyRequests: TooManyRequests) {
            if (retryNumber <= 5) {
                logger.warn("429 error reached: try #$retryNumber, chatId = $chatId, text = $text")
                coroutineScope {
                    launch {
                        delay(5_000)
                        sendMessage(chatId, text, replyToMessageId, retryNumber + 1)
                    }
                }
            } else {
                tooManyRequests.printStackTrace()
            }
        }
    }

    suspend fun sendToCurrentChat(text: String) {
        val update = getCurrentUpdate()
        update.getContextChatId()?.let {
            sendMessage(it, text)
        } ?: errorStorageService.logAndReportError(logger, "Cannot send message reply to current chat: $text", update)
    }

    suspend fun replyToCurrentMessage(text: String) {

        val update = getCurrentUpdate()
        update.getContextChatId()?.let {
            sendMessage(it, text, update.getContextMessageId())
        } ?: errorStorageService.logAndReportError(
            logger,
            "Cannot send message reply to current message: $text",
            update
        )
    }

    private fun apiFor(tgBot: TgBot): String {
        return "https://api.telegram.org/bot${tgBotConfig.getBotInfo(tgBot)!!.token}"
    }

}