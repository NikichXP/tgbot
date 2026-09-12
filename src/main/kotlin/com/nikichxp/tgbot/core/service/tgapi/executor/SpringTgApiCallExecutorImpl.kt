package com.nikichxp.tgbot.core.service.tgapi.executor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.service.TgBotV2Service
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import kotlin.time.Duration.Companion.seconds

@Deprecated("just a fallback for now")
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

    override suspend fun callEndpointMultipart(
        tgBot: TgBotInfo,
        method: String,
        parts: List<TgMultipartPart>
    ): TgApiResponse {
        return try {
            val entity = executeMultipart(tgBot, method, parts, retryNumber = 0)
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

    private suspend fun executeMultipart(
        tgBot: TgBotInfo,
        method: String,
        parts: List<TgMultipartPart>,
        retryNumber: Int
    ): ResponseEntity<JsonNode> {
        try {
            val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
            parts.forEach { part ->
                when (part) {
                    is TgMultipartPart.Text -> body.add(part.name, part.value)
                    is TgMultipartPart.FilePart -> {
                        val resource = object : ByteArrayResource(part.content) {
                            override fun getFilename() = part.fileName
                        }
                        val fileHeaders = HttpHeaders().apply { contentType = MediaType.parseMediaType(part.contentType) }
                        body.add(part.name, HttpEntity(resource, fileHeaders))
                    }
                }
            }
            return restTemplate.postForEntity<JsonNode>(
                "${tgBotV2Service.getBaseApiFor(tgBot)}/$method",
                request = body
            )
        } catch (tooManyRequests: HttpClientErrorException.TooManyRequests) {
            logger.warn("429 error reached: try #$retryNumber, message = $tooManyRequests")
            if (retryNumber < appConfig.maxRetryCount) {
                delay(1.seconds)
                return executeMultipart(tgBot, method, parts, retryNumber + 1)
            }
            throw tooManyRequests
        }
    }

    private fun handleError(exception: HttpStatusCodeException, method: String): TgApiResponse {
        val errorBody = runCatching { objectMapper.readTree(exception.responseBodyAsString) }
            .getOrDefault(objectMapper.createObjectNode())
        logger.error("Tg API error: status={}, method={}, body={}", exception.statusCode, method, errorBody)

        val description = errorBody.get("description")?.asText().orEmpty()

        val responseStatus = when {
            exception is HttpClientErrorException.TooManyRequests -> TgResponseStatus.TOO_MANY_REQUESTS
            exception.statusCode == HttpStatus.CONFLICT -> TgResponseStatus.CONFLICT
            description.contains("message is too long", ignoreCase = true) -> TgResponseStatus.MESSAGE_TOO_LONG
            else -> TgResponseStatus.UNKNOWN_ERROR
        }

        return TgApiResponse(errorBody, success = false, responseStatus = responseStatus)
    }

}
