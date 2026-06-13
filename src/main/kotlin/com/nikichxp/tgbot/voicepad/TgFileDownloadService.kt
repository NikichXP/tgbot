package com.nikichxp.tgbot.voicepad

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.dto.TgGetFileResponse
import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2
import com.nikichxp.tgbot.core.service.TgBotV2Service
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TgFileDownloadService(
    private val client: HttpClient,
    private val tgBotV2Service: TgBotV2Service,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun downloadVoice(fileId: String, bot: TgBotInfoV2): ByteArray {
        val token = tgBotV2Service.getTokenById(bot.name)
        val apiBase = "$TG_API_BASE$token"

        val getFileResponse = client.post("$apiBase$GET_FILE_ENDPOINT") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(FILE_ID_PARAM to fileId))
        }.body<String>()

        val fileInfo = objectMapper.readValue(getFileResponse, TgGetFileResponse::class.java)
        val filePath = fileInfo.result?.filePath
            ?: error("getFile returned no file_path for fileId=$fileId")

        logger.info("Downloading voice file: {}", filePath)
        return client.get("$TG_FILE_BASE$token/$filePath").body()
    }

    companion object {
        private const val TG_API_BASE = "https://api.telegram.org/bot"
        private const val TG_FILE_BASE = "https://api.telegram.org/file/bot"
        private const val GET_FILE_ENDPOINT = "/getFile"
        private const val FILE_ID_PARAM = "file_id"
    }
}
