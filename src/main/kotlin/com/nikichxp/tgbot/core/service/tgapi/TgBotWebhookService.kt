package com.nikichxp.tgbot.core.service.tgapi

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2
import com.nikichxp.tgbot.core.service.TgBotV2Service
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TgBotWebhookService(
    private val client: HttpClient,
    private val tgBotV2Service: TgBotV2Service,
    appConfig: AppConfig
) {

    private var webHookUrl = appConfig.webhook
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun register(botInfo: TgBotInfoV2): Boolean {
        val response = postCallWith(apiUrl(botInfo, Operation.SET_WEBHOOK), mapOf("url" to "$webHookUrl/${botInfo.name}"))
        val status = response.status.value in 200..299
        logger.info(formatLog(botInfo, "Register webhook status $status with message: ${response.body<String>()}"))
        return status
    }

    suspend fun unregister(botInfo: TgBotInfoV2): Boolean {
        val response = postCallWith(apiUrl(botInfo, Operation.DELETE_WEBHOOK), mapOf("drop_pending_updates" to false.toString()))
        val status = response.status.value in 200..299
        logger.info(formatLog(botInfo, "Unregister webhook status $status with message: ${response.body<String>()}"))
        return status
    }

    private fun formatLog(botInfo: TgBotInfoV2, message: String): String = "Bot = ${botInfo.name}, message = $message"

    private fun apiUrl(botInfo: TgBotInfoV2, operation: Operation): String {
        val token = tgBotV2Service.getTokenById(botInfo.name)
        return "https://api.telegram.org/bot$token/${operation.endpoint}"
    }

    private suspend fun postCallWith(url: String, args: Map<String, String>): HttpResponse {
        val params = Parameters.build {
            for ((k, v) in args) {
                this.append(k, v)
            }
        }
        return client.submitForm(url, params)
    }

    private enum class Operation(val endpoint: String) {
        SET_WEBHOOK("setWebhook"),
        DELETE_WEBHOOK("deleteWebhook")
    }

}