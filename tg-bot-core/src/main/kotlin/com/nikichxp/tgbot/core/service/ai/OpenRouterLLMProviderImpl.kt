package com.nikichxp.tgbot.core.service.ai

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.ai.LLMRequest
import com.nikichxp.tgbot.core.dto.ai.LLMResponse
import com.nikichxp.tgbot.core.dto.ai.LLMUsage
import com.nikichxp.tgbot.core.dto.ai.OpenRouterChatRequest
import com.nikichxp.tgbot.core.dto.ai.OpenRouterChatResponse
import com.nikichxp.tgbot.core.dto.ai.OpenRouterMessage
import com.nikichxp.tgbot.core.dto.ai.OpenRouterModelsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OpenRouterLLMProviderImpl(
    private val client: HttpClient,
    appConfig: AppConfig
) : ILLMProvider {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val config = appConfig.openRouter

    override val name: String = "openrouter"

    override suspend fun complete(request: LLMRequest): LLMResponse {
        check(config.apiKey.isNotBlank()) { "app.openrouter.api-key is not configured" }

        val payload = OpenRouterChatRequest(
            model = request.model ?: config.defaultModel,
            messages = request.messages.map {
                OpenRouterMessage(role = it.role.name.lowercase(), content = it.content)
            },
            temperature = request.temperature,
            maxTokens = request.maxTokens
        )

        val response = client.post("${config.baseUrl}/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
            if (config.referer.isNotBlank()) header("HTTP-Referer", config.referer)
            if (config.title.isNotBlank()) header("X-Title", config.title)
            contentType(ContentType.Application.Json)
            setBody(payload)
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.bodyAsText() }.getOrDefault("")
            logger.warn("OpenRouter call failed: status={}, body={}", response.status, body)
            error("OpenRouter request failed with status ${response.status}: $body")
        }

        val parsed: OpenRouterChatResponse = response.body()
        val choice = parsed.choices.firstOrNull()
            ?: error("OpenRouter response contained no choices")

        return LLMResponse(
            content = choice.message.content.orEmpty(),
            model = parsed.model ?: payload.model,
            usage = parsed.usage?.let {
                LLMUsage(
                    promptTokens = it.promptTokens ?: 0,
                    completionTokens = it.completionTokens ?: 0,
                    totalTokens = it.totalTokens ?: 0
                )
            }
        )
    }

    override suspend fun listModels(): List<String> {
        check(config.apiKey.isNotBlank()) { "app.openrouter.api-key is not configured" }

        val response = client.get("${config.baseUrl}/models") {
            header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.bodyAsText() }.getOrDefault("")
            logger.warn("OpenRouter models call failed: status={}, body={}", response.status, body)
            error("OpenRouter models request failed with status ${response.status}: $body")
        }

        val parsed: OpenRouterModelsResponse = response.body()
        return parsed.data.map { it.id }
    }
}
