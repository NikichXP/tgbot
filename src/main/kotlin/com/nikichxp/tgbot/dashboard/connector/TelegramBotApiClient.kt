package com.nikichxp.tgbot.dashboard.connector

import com.fasterxml.jackson.databind.JsonNode
import com.nikichxp.tgbot.dashboard.dto.TgBotIdentity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.springframework.stereotype.Component

/**
 * TODO: This whole logic must be unified at some point, out of scope for now
 */
@Component
class TelegramBotApiClient(
    private val httpClient: HttpClient
) {

    suspend fun getMe(token: String): TgBotIdentity? {
        val response = httpClient.get("https://api.telegram.org/bot$token/getMe").body<JsonNode>()
        if (!response.path("ok").asBoolean(false)) return null
        val result = response.path("result")
        return TgBotIdentity(result.path("id").asLong(), result.path("username").asText())
    }
}
