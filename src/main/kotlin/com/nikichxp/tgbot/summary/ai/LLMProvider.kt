package com.nikichxp.tgbot.summary.ai

interface LLMProvider {

    val name: String

    suspend fun complete(request: LLMRequest): LLMResponse
}

enum class LLMRole {
    SYSTEM, USER, ASSISTANT
}

data class LLMMessage(
    val role: LLMRole,
    val content: String
)

data class LLMRequest(
    val messages: List<LLMMessage>,
    val model: String? = null,
    val temperature: Double? = null,
    val maxTokens: Int? = null
) {
    companion object {
        fun of(
            systemPrompt: String?,
            userPrompt: String,
            model: String? = null,
            temperature: Double? = null,
            maxTokens: Int? = null
        ): LLMRequest {
            val messages = buildList {
                if (!systemPrompt.isNullOrBlank()) add(LLMMessage(LLMRole.SYSTEM, systemPrompt))
                add(LLMMessage(LLMRole.USER, userPrompt))
            }
            return LLMRequest(
                messages = messages,
                model = model,
                temperature = temperature,
                maxTokens = maxTokens
            )
        }
    }
}

data class LLMResponse(
    val content: String,
    val model: String,
    val usage: LLMUsage? = null
)

data class LLMUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)
