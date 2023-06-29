package com.nikichxp.tgbot.service.tgapi

import com.nikichxp.tgbot.config.AppConfig
import com.nikichxp.tgbot.entity.BotInfo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TgBotSetWebhookService(
    private val client: HttpClient,
    appConfig: AppConfig
) {

    private var webHookUrl = appConfig.webhook
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun register(botInfo: BotInfo): Boolean {
        val response = postCallWith(apiUrl(botInfo), mapOf("url" to "$webHookUrl/${botInfo.name}"))
        val status = response.status.value in 200..299
        logger.info(formatLog(botInfo, "Register state $status with message: ${response.body<String>()}"))
        return status
    }

    private fun formatLog(botInfo: BotInfo, message: String): String = "Bot = ${botInfo.name}, message = $message"

    private fun apiUrl(botInfo: BotInfo): String {
        return "https://api.telegram.org/bot${botInfo.token}/setWebhook"
    }

    private suspend fun postCallWith(url: String, args: Map<String, String>): HttpResponse {
        val params = Parameters.build {
            for ((k, v) in args) {
                this.append(k, v)
            }
        }
        return client.submitForm(url, params)
    }

}