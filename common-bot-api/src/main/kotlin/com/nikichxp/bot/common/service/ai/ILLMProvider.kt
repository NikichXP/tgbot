package com.nikichxp.bot.common.service.ai

import com.nikichxp.bot.common.dto.ai.LLMRequest
import com.nikichxp.bot.common.dto.ai.LLMResponse

interface ILLMProvider {

    val name: String

    suspend fun complete(request: LLMRequest): LLMResponse

    suspend fun listModels(): List<String>
}
