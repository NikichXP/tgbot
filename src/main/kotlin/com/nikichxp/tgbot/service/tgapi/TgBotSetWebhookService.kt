package com.nikichxp.tgbot.service.tgapi

import com.nikichxp.tgbot.config.AppConfig
import com.nikichxp.tgbot.entity.BotInfo
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Service
class TgBotSetWebhookService(
    private val restTemplate: RestTemplate,
    appConfig: AppConfig
) {

    private var webHookUrl = appConfig.webhook
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun register(botInfo: BotInfo): Boolean {
        val result = try {
            val response = postCallWith(apiUrl(botInfo), mapOf("url" to "$webHookUrl/${botInfo.name}"))
            logger.info(formatLog(botInfo, "Register ok with message: ${response.body}"))
            return true
        } catch (exception: HttpClientErrorException) {
            exception.message ?: "No error data provided"
        } catch (generalException: Throwable) {
            "Non-web related error thrown: ${generalException.message}"
        }
        logger.warn(formatLog(botInfo, result))
        return false
    }

    private fun formatLog(botInfo: BotInfo, message: String): String = "Bot = ${botInfo.name}, message = $message"

    private fun apiUrl(botInfo: BotInfo): String {
        return "https://api.telegram.org/bot${botInfo.token}/setWebhook"
    }

    private fun postCallWith(url: String, args: Map<String, Any>): ResponseEntity<String> {
        return restTemplate.postForEntity<String>(url, args)
    }

}