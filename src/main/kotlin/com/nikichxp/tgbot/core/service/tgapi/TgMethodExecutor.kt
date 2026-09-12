package com.nikichxp.tgbot.core.service.tgapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.entity.bots.BotInfo
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.service.TgBotV2Service
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Primary
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
@ConditionalOnMissingBean(TgApiCallExecutor::class)
class SpringTgApiCallExecutorImpl(
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

@Service
@Primary
class KtorTgApiCallExecutorImpl(
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper,
    tgBotV2Service: TgBotV2Service
): TgApiCallExecutor(tgBotV2Service) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun callEndpoint(
        tgBot: TgBotInfo,
        method: String,
        parameters: Any
    ): TgApiResponse {
        return execute(tgBot, method, parameters, retryNumber = 0)
    }

    private suspend fun execute(tgBot: TgBotInfo, method: String, parameters: Any, retryNumber: Int): TgApiResponse {
        val body = objectMapper.valueToTree<JsonNode>(parameters)
        val response = httpClient.post("${apiFor(tgBot.name)}/$method") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        if (response.status.value >= 400) {
            val errorBody = response.body<JsonNode>()
            logger.error("Tg API error: status={}, method={}, body={}", response.status, method, errorBody)

            if (response.status == HttpStatusCode.TooManyRequests) {
                if (retryNumber < MAX_RETRIES) {
                    delay(1.seconds)
                    return execute(tgBot, method, parameters, retryNumber + 1)
                }
                return TgApiResponse(errorBody, success = false, responseType = TgResponseType.TOO_MANY_REQUESTS)
            }

            val description = errorBody.get("description")?.asText().orEmpty()
            if (description.contains("message is too long", ignoreCase = true)) {
                return TgApiResponse(errorBody, success = false, responseType = TgResponseType.MESSAGE_TOO_LONG)
            }

            return TgApiResponse(errorBody, success = false, responseType = TgResponseType.UNKNOWN_ERROR)
        }

        return TgApiResponse(response.body<JsonNode>())
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
    MESSAGE_TOO_LONG,
    UNKNOWN_ERROR
}






















