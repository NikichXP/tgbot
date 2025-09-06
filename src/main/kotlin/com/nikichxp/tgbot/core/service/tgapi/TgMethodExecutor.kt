package com.nikichxp.tgbot.core.service.tgapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2
import com.nikichxp.tgbot.core.service.TgBotV2Service
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException.TooManyRequests
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Service
class TgMethodExecutor(
    private val objectMapper: ObjectMapper,
    private val restTemplate: RestTemplate,
    private val tgBotV2Service: TgBotV2Service
) {

    private val logger = LoggerFactory.getLogger(TgMethodExecutor::class.java)

    suspend fun execute(tgBot: TgBotInfoV2, method: String, parameters: Any): ResponseEntity<JsonNode> {
        return execute(tgBot, method, parameters, 0)
    }

    private suspend fun execute(tgBot: TgBotInfoV2, method: String, parameters: Any, retryNumber: Int): ResponseEntity<JsonNode> {
        try {
            val body = objectMapper.valueToTree<JsonNode>(parameters)
            return restTemplate.postForEntity<JsonNode>(
                "${apiFor(tgBot.name)}/$method",
                request = body
            )
        } catch (tooManyRequests: TooManyRequests) {
            logger.warn("429 error reached: try #$retryNumber, message = $tooManyRequests")
            if (retryNumber < MAX_RETRIES) {
                delay(1000)
                return execute(tgBot, method, parameters, retryNumber + 1)
            }
            throw tooManyRequests
        }
    }

    private fun apiFor(tgBotName: String): String {
        return "https://api.telegram.org/bot${tgBotV2Service.getTokenById(tgBotName)}"
    }

    companion object {
        private const val MAX_RETRIES = 5
    }
}