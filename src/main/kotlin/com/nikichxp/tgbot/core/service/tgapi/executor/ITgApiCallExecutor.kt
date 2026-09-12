package com.nikichxp.tgbot.core.service.tgapi.executor

import com.nikichxp.tgbot.core.entity.bots.TgBotInfo

interface ITgApiCallExecutor {
    suspend fun callEndpoint(tgBot: TgBotInfo, method: String, parameters: Any): TgApiResponse
    suspend fun callEndpointMultipart(tgBot: TgBotInfo, method: String, parts: List<TgMultipartPart>): TgApiResponse
}
