package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.config.AppConfig
import com.nikichxp.tgbot.core.CurrentUpdateProvider
import com.nikichxp.tgbot.util.getContextChatId
import com.nikichxp.tgbot.util.getContextMessageId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity
import javax.annotation.PostConstruct

@Service
class TgOperations(
    private val updateProvider: CurrentUpdateProvider,
    private val restTemplate: RestTemplate,
    private val appConfig: AppConfig
) {

    @Value("\${TG_TOKEN}")
    private lateinit var token: String

    private lateinit var apiUrl: String

    @PostConstruct
    fun registerWebhook() {
        apiUrl = "https://api.telegram.org/bot$token"
        if (appConfig.appName.isEmpty()) return
        val response = restTemplate.getForEntity<String>(
            apiUrl + "/setWebhook?url=${generateUrl()}"
        )
        println(response)
    }


    fun sendMessage(chatId: Long, text: String, replyToMessageId: Long? = null) {
        val args = mutableListOf<Pair<String, Any>>(
            "chat_id" to chatId,
            "text" to text
        )

        replyToMessageId?.apply { args += "reply_to_message_id" to replyToMessageId }

        restTemplate.postForEntity<String>("$apiUrl/sendMessage", args.toMap())
    }

    fun replyToCurrentMessage(text: String) {
        updateProvider.update?.getContextChatId()?.let {
            sendMessage(it, text, updateProvider.update?.getContextMessageId())
        } ?: logger.warn("Cannot send message reply in: $text")
    }

    private fun generateUrl(): String = "https://${appConfig.appName}.herokuapp.com/handle"

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}