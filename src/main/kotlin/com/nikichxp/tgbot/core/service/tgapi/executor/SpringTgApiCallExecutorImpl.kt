package com.nikichxp.tgbot.core.service.tgapi.executor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.service.TgBotV2Service
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import kotlin.time.Duration.Companion.seconds

@Service
@ConditionalOnMissingBean(ITgApiCallExecutor::class)
class SpringTgApiCallExecutorImpl(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    private val appConfig: AppConfig,
    private val tgBotV2Service: TgBotV2Service
): ITgApiCallExecutor {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun callEndpoint(
        tgBot: TgBotInfo,
        method: String,
        parameters: Any
    ): TgApiResponse {
        return try {
            val entity = execute(tgBot, method, parameters, retryNumber = 0)
            TgApiResponse(entity.body!!)
        } catch (exception: HttpStatusCodeException) {
            handleError(exception, method)
        }
    }

    private suspend fun execute(tgBot: TgBotInfo, method: String, parameters: Any, retryNumber: Int): ResponseEntity<JsonNode> {
        try {
            val body = objectMapper.valueToTree<JsonNode>(parameters)
            return restTemplate.postForEntity<JsonNode>(
                "${tgBotV2Service.getBaseApiFor(tgBot)}/$method",
                request = body
            )
        } catch (tooManyRequests: HttpClientErrorException.TooManyRequests) {
            logger.warn("429 error reached: try #$retryNumber, message = $tooManyRequests")
            if (retryNumber < appConfig.maxRetryCount) {
                delay(1.seconds)
                return execute(tgBot, method, parameters, retryNumber + 1)
            }
            throw tooManyRequests
        }
    }

    private fun handleError(exception: HttpStatusCodeException, method: String): TgApiResponse {
        val errorBody = runCatching { objectMapper.readTree(exception.responseBodyAsString) }
            .getOrDefault(objectMapper.createObjectNode())
        logger.error("Tg API error: status={}, method={}, body={}", exception.statusCode, method, errorBody)

        val description = errorBody.get("description")?.asText().orEmpty()

        val responseType = when {
            exception is HttpClientErrorException.TooManyRequests -> TgResponseType.TOO_MANY_REQUESTS
            description.contains("message is too long", ignoreCase = true) -> TgResponseType.MESSAGE_TOO_LONG
            else -> TgResponseType.UNKNOWN_ERROR
        }

        return TgApiResponse(errorBody, success = false, responseType = responseType)
    }

}
