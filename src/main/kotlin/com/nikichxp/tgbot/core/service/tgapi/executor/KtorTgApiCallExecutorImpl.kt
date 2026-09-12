package com.nikichxp.tgbot.core.service.tgapi.executor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.service.TgBotV2Service
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.seconds

@Service
@Primary
class KtorTgApiCallExecutorImpl(
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper,
    private val appConfig: AppConfig,
    private val tgBotV2Service: TgBotV2Service
) : ITgApiCallExecutor {

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
        val response = httpClient.post("${tgBotV2Service.getBaseApiFor(tgBot)}/$method") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        return when (response.status.value) {
            HttpStatusCode.TooManyRequests.value -> {
                if (retryNumber < appConfig.maxRetryCount) {
                    delay(1.seconds)
                    return execute(tgBot, method, parameters, retryNumber + 1)
                }
                handleError(response, method, TgResponseType.TOO_MANY_REQUESTS, response.body<JsonNode>())
            }
            in 400..599 -> {
                val errorBody = response.body<JsonNode>()
                val description = errorBody.get("description")?.asText().orEmpty()
                if (description.contains("message is too long", ignoreCase = true)) {
                    handleError(response, method, TgResponseType.MESSAGE_TOO_LONG, errorBody)
                } else {
                    handleError(response, method, TgResponseType.UNKNOWN_ERROR, errorBody)
                }
            }
            in 200..299 -> TgApiResponse(response.body<JsonNode>())
            else -> throw IllegalStateException("Unexpected server response")
        }
    }

    private fun handleError(
        response: HttpResponse,
        method: String,
        responseType: TgResponseType,
        errorBody: JsonNode
    ): TgApiResponse {
        logger.error("Tg API error: status={}, method={}, body={}", response.status, method, errorBody)

        return TgApiResponse(errorBody, success = false, responseType = responseType)
    }

}
