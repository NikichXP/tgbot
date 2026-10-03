package com.nikichxp.tgbot.people.connector

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.nikichxp.tgbot.core.dto.files.PhotoSize
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.service.TgBotV2Service
import com.nikichxp.tgbot.core.service.tgapi.executor.ITgApiCallExecutor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

@Component
class TelegramProfilePhotoConnector(
    private val tgApiCallExecutor: ITgApiCallExecutor,
    private val tgBotV2Service: TgBotV2Service,
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper
) {

    suspend fun downloadCurrentProfilePhoto(bot: TgBotInfo, userId: Long): ByteArray? {
        val photo = currentProfilePhotoSizes(bot, userId).closestTo(PREFERRED_SIZE_PX) ?: return null
        val filePath = filePath(bot, photo.fileId) ?: return null
        val token = withContext(Dispatchers.IO) { tgBotV2Service.getTokenById(bot.name) }
        val response = httpClient.get("$TELEGRAM_FILE_BASE_URL$token/$filePath")
        return if (response.status.isSuccess()) response.body() else null
    }

    private suspend fun currentProfilePhotoSizes(bot: TgBotInfo, userId: Long): List<PhotoSize> {
        val response = tgApiCallExecutor.callEndpoint(
            bot, "getUserProfilePhotos", mapOf("user_id" to userId, "limit" to 1)
        )
        if (!response.success) return emptyList()
        val currentPhoto = response.content.path("result").path("photos").path(0)
        return if (currentPhoto.isArray) objectMapper.convertValue(currentPhoto) else emptyList()
    }

    private suspend fun filePath(bot: TgBotInfo, fileId: String): String? {
        val response = tgApiCallExecutor.callEndpoint(bot, "getFile", mapOf("file_id" to fileId))
        if (!response.success) return null
        return response.content.path("result").path("file_path").takeIf { it.isTextual }?.asText()
    }

    private fun List<PhotoSize>.closestTo(sizePx: Int): PhotoSize? =
        filter { it.width >= sizePx }.minByOrNull { it.width } ?: maxByOrNull { it.width }

    companion object {
        private const val TELEGRAM_FILE_BASE_URL = "https://api.telegram.org/file/bot"
        private const val PREFERRED_SIZE_PX = 160
    }
}
