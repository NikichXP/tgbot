package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.config.AppConfig
import com.nikichxp.tgbot.config.ApplicationBeans
import com.nikichxp.tgbot.core.CurrentUpdateProvider
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.entity.TgBotConfig
import com.nikichxp.tgbot.util.getContextChatId
import com.nikichxp.tgbot.util.getContextMessageId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException.TooManyRequests
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct


@Service
class TgOperations(
    private val updateProvider: CurrentUpdateProvider,
    private val restTemplate: RestTemplate,
    appConfig: AppConfig,
    private val tgBotConfig: TgBotConfig
) {

    private val bots = tgBotConfig.getInitializedBots()
    private var webHookUrl = appConfig.webhook

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val scheduler = Executors.newScheduledThreadPool(1)

    @PostConstruct
    fun registerWebhooks() {
        logger.info("Registering bots: ${bots.map { it.bot }}")

        bots.forEach { bot ->
            val response = restTemplate.getForEntity<String>(
                "${apiFor(bot.bot)}/setWebhook?url=$webHookUrl/${bot.name}"
            )
            println(response)
        }
    }

    fun apiFor(): String {
        return apiFor(updateProvider.bot)
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