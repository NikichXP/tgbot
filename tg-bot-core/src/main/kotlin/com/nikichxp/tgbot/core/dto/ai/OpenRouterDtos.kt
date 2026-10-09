package com.nikichxp.tgbot.core.dto.ai

import com.fasterxml.jackson.annotation.JsonProperty

internal data class OpenRouterModelsResponse(
    val data: List<OpenRouterModelInfo> = emptyList()
)

internal data class OpenRouterModelInfo(
    val id: String
)

internal data class OpenRouterChatRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val temperature: Double? = null,
    @JsonProperty("max_tokens") val maxTokens: Int? = null
)

internal data class OpenRouterMessage(
    val role: String,
    val content: String
)

internal data class OpenRouterChatResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<OpenRouterChoice> = emptyList(),
    val usage: OpenRouterUsage? = null
)

internal data class OpenRouterChoice(
    val index: Int? = null,
    val message: OpenRouterResponseMessage,
    @JsonProperty("finish_reason") val finishReason: String? = null
)

internal data class OpenRouterResponseMessage(
    val role: String? = null,
    val content: String? = null
)

internal data class OpenRouterUsage(
    @JsonProperty("prompt_tokens") val promptTokens: Int? = null,
    @JsonProperty("completion_tokens") val completionTokens: Int? = null,
    @JsonProperty("total_tokens") val totalTokens: Int? = null
)
