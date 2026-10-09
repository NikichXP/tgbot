package com.nikichxp.tgbot.core.service.ai

import com.nikichxp.tgbot.core.dto.ai.LLMRequest
import com.nikichxp.tgbot.core.dto.ai.LLMResponse

interface ILLMProvider {

    val name: String

    suspend fun complete(request: LLMRequest): LLMResponse

    suspend fun listModels(): List<String>
}
