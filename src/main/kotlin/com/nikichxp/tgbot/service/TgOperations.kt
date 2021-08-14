package com.nikichxp.tgbot.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import javax.annotation.PostConstruct

@Service
class TgOperations(
        private val restTemplate: RestTemplate
) {

    @Value("\${TG_TOKEN}")
    private lateinit var token: String

    // @Value("\${API_URL:}")
    // currently not set
    private var url: String = ""

    @PostConstruct
    fun registerWebhook() {
        if (url.isEmpty()) return
        val response = restTemplate.getForEntity<String>("https://api.telegram.org/bot$token/setWebhook?url=$url")
        println(response)
    }

}