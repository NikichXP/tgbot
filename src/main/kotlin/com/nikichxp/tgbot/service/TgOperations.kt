package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.config.AppConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import javax.annotation.PostConstruct

@Service
class TgOperations(
    private val restTemplate: RestTemplate,
    private val appConfig: AppConfig
) {

    @Value("\${TG_TOKEN}")
    private lateinit var token: String

    @PostConstruct
    fun registerWebhook() {
        if (appConfig.appName.isEmpty()) return
        val response = restTemplate.getForEntity<String>(
            "https://api.telegram.org/" +
                    "bot$token/" +
                    "setWebhook?url=${generateUrl()}"
        )
        println(response)
    }

    private fun generateUrl(): String = "https://${appConfig.appName}.herokuapp.com/handle"

}