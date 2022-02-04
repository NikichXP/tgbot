package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.config.AppConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity
import javax.annotation.PostConstruct

@Service
class TgOperations(
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

    /**
     * Send message to chat
     * @param chatId can be user/group id, or @nickname
     */
    fun sendMessage(chatId: String, text: String) {
        restTemplate.postForEntity<String>(apiUrl + "", mapOf(
            "chat_id" to chatId,
            "text" to text
        ))
    }

    private fun generateUrl(): String = "https://${appConfig.appName}.herokuapp.com/handle"

}