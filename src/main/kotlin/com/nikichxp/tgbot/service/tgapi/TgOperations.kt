package com.nikichxp.tgbot.service.tgapi

import com.nikichxp.tgbot.core.CurrentUpdateProvider
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.entity.TgBotConfig
import com.nikichxp.tgbot.util.getContextChatId
import com.nikichxp.tgbot.util.getContextMessageId
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException.TooManyRequests
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct


@Service
class TgOperations(
    private val updateProvider: CurrentUpdateProvider,
    private val restTemplate: RestTemplate,
    private val tgSetWebhookService: TgBotSetWebhookService,
    private val tgUpdatePollService: TgUpdatePollService,
    private val tgBotConfig: TgBotConfig
) {

    private val bots = tgBotConfig.getInitializedBots()

    private val scheduler = Executors.newScheduledThreadPool(1)

    @PostConstruct
    fun registerWebhooks() {
        logger.info("Registering bots: ${bots.map { it.bot }}")
        bots.forEach {
            val webhookSet = runBlocking { tgSetWebhookService.register(it) }
            if (!webhookSet) {
                tgUpdatePollService.startPollingFor(it)
            }
        }
    }

    // TODO Delete this
    fun apiFor(): String {
        return apiFor(updateProvider.bot)
    }

    fun apiFor(update: Update): String {
        return apiFor(update.bot)
    }

    fun apiFor(tgBot: TgBot): String {
        return "https://api.telegram.org/bot${tgBotConfig.getBotInfo(tgBot)!!.token}"
    }

    fun sendMessage(chatId: Long, text: String, replyToMessageId: Long? = null, retryNumber: Int = 0) {
        val args = mutableListOf<Pair<String, Any>>(
            "chat_id" to chatId,
            "text" to text
        )

        replyToMessageId?.apply { args += "reply_to_message_id" to replyToMessageId }

        try {
            restTemplate.postForEntity<String>("${apiFor()}/sendMessage", args.toMap())
        } catch (tooManyRequests: TooManyRequests) {
            if (retryNumber <= 5) {
                logger.warn("429 error reached: try #$retryNumber, chatId = $chatId, text = $text")
                scheduler.schedule(
                    { sendMessage(chatId, text, replyToMessageId, retryNumber + 1) },
                    1,
                    TimeUnit.MINUTES
                )
            } else {
                tooManyRequests.printStackTrace()
            }
        }
    }

    fun sendToCurrentChat(text: String) {
        updateProvider.update?.getContextChatId()?.let {
            sendMessage(it, text)
        } ?: logger.warn("Cannot send message reply in: $text")
    }

    fun replyToCurrentMessage(text: String) {
        updateProvider.update?.getContextChatId()?.let {
            sendMessage(it, text, updateProvider.update?.getContextMessageId())
        } ?: logger.warn("Cannot send message reply in: $text")
    }


    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}