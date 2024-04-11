package com.nikichxp.tgbot.service.tgapi

import com.nikichxp.tgbot.config.AppConfig
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.entity.TgBotConfig
import com.nikichxp.tgbot.entity.UpdateContext
import com.nikichxp.tgbot.util.getContextChatId
import com.nikichxp.tgbot.util.getContextMessageId
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException.TooManyRequests
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class TgOperations(
    private val restTemplate: RestTemplate,
    private val tgSetWebhookService: TgBotSetWebhookService,
    private val tgUpdatePollService: TgUpdatePollService,
    private val tgBotConfig: TgBotConfig,
    private val appConfig: AppConfig
) {

    private val bots = tgBotConfig.getInitializedBots()
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val scheduler = Executors.newScheduledThreadPool(1)

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

    fun apiFor(updateContext: UpdateContext) = apiFor(updateContext.tgBot)

    fun apiFor(update: Update) = apiFor(update.bot)

    fun apiFor(tgBot: TgBot): String {
        return "https://api.telegram.org/bot${tgBotConfig.getBotInfo(tgBot)!!.token}"
    }

    fun deleteWebhook(tgBot: TgBot) {
        restTemplate.getForEntity<String>(apiFor(tgBot) + "/deleteWebhook").body
    }

    fun sendMessage(chatId: Long, text: String, update: Update, replyToMessageId: Long? = null, retryNumber: Int = 0) {
        val args = mutableListOf<Pair<String, Any>>(
            "chat_id" to chatId,
            "text" to text
        )

        replyToMessageId?.apply { args += "reply_to_message_id" to replyToMessageId }

        try {
            restTemplate.postForEntity<String>("${apiFor(update)}/sendMessage", args.toMap())
        } catch (tooManyRequests: TooManyRequests) {
            if (retryNumber <= 5) {
                logger.warn("429 error reached: try #$retryNumber, chatId = $chatId, text = $text")
                scheduler.schedule(
                    { sendMessage(chatId, text, update, replyToMessageId, retryNumber + 1) },
                    5,
                    TimeUnit.SECONDS
                )
            } else {
                tooManyRequests.printStackTrace()
            }
        }
    }

    fun sendToCurrentChat(text: String, update: Update) {
        update.getContextChatId()?.let {
            sendMessage(it, text, update)
        } ?: logger.warn("Cannot send message reply in: $text")
    }

    fun replyToCurrentMessage(text: String, update: Update) {
        update.getContextChatId()?.let {
            sendMessage(it, text, update, update.getContextMessageId())
        } ?: logger.warn("Cannot send message reply in: $text")
    }

    companion object {
    }
}