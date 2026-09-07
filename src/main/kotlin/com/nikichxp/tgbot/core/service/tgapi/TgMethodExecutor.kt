package com.nikichxp.tgbot.core.service.tgapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.entity.bots.BotInfo
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.service.TgBotV2Service
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException.TooManyRequests
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import kotlin.time.Duration.Companion.seconds

private const val MAX_RETRIES = 5


@Service
class TgMethodExecutor(
    private val objectMapper: ObjectMapper,
    private val restTemplate: RestTemplate,
    private val tgBotV2Service: TgBotV2Service
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun execute(tgBot: BotInfo, method: String, parameters: Any): ResponseEntity<JsonNode> {
        require(tgBot is TgBotInfo) {"TgMethodExecutor can only execute methods for TgBotInfo"}
        return execute(tgBot, method, parameters, 0)
    }

    private suspend fun execute(tgBot: TgBotInfo, method: String, parameters: Any, retryNumber: Int): ResponseEntity<JsonNode> {
        try {
            val body = objectMapper.valueToTree<JsonNode>(parameters)
            return restTemplate.postForEntity<JsonNode>(
                "${apiFor(tgBot.name)}/$method",
                request = body
            )
        } catch (tooManyRequests: TooManyRequests) {
            logger.warn("429 error reached: try #$retryNumber, message = $tooManyRequests")
            if (retryNumber < MAX_RETRIES) {
                delay(1.seconds)
                return execute(tgBot, method, parameters, retryNumber + 1)
            }
            throw tooManyRequests
        }
    }

    private fun apiFor(tgBotName: String): String {
        return "https://api.telegram.org/bot${tgBotV2Service.getTokenById(tgBotName)}"
    }

}

abstract class TgApiCallExecutor(
    private val tgBotV2Service: TgBotV2Service
) {
    abstract suspend fun callEndpoint(tgBot: TgBotInfo, method: String, parameters: Any): TgApiResponse

    protected fun apiFor(tgBotName: String): String {
        return "https://api.telegram.org/bot${tgBotV2Service.getTokenById(tgBotName)}"
    }
}

@Service
class SpringWebApiCallExecutor(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    tgBotV2Service: TgBotV2Service
): TgApiCallExecutor(tgBotV2Service) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun callEndpoint(
        tgBot: TgBotInfo,
        method: String,
        parameters: Any
    ): TgApiResponse {
        try {
            val entity = execute(tgBot, method, parameters, retryNumber = 0)
            return TgApiResponse(entity.body!!)
        } catch (exception: TooManyRequests) {
            return TgApiResponse(
                objectMapper.createObjectNode(),
                success = false,
                responseType = TgResponseType.TOO_MANY_REQUESTS
            )
        }
    }

    private suspend fun execute(tgBot: TgBotInfo, method: String, parameters: Any, retryNumber: Int): ResponseEntity<JsonNode> {
        try {
            val body = objectMapper.valueToTree<JsonNode>(parameters)
            return restTemplate.postForEntity<JsonNode>(
                "${apiFor(tgBot.name)}/$method",
                request = body
            )
        } catch (tooManyRequests: TooManyRequests) {
            logger.warn("429 error reached: try #$retryNumber, message = $tooManyRequests")
            if (retryNumber < MAX_RETRIES) {
                delay(1.seconds)
                return execute(tgBot, method, parameters, retryNumber + 1)
            }
            throw tooManyRequests
        }
    }

}

data class TgApiResponse(
    val content: JsonNode,
    val success: Boolean = true,
    val responseType: TgResponseType = TgResponseType.OK
)

enum class TgResponseType {
    OK,
    TOO_MANY_REQUESTS,
    MESSAGE_TOO_LONG
}






















