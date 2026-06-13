package com.nikichxp.tgbot.voicepad

import com.nikichxp.tgbot.core.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class VoiceTranscriptionService(
    private val client: HttpClient,
    appConfig: AppConfig
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val config = appConfig.openRouter

    suspend fun transcribe(audioBytes: ByteArray, mimeType: String = DEFAULT_MIME_TYPE): String {
        check(config.apiKey.isNotBlank()) { "app.openrouter.api-key is not configured" }

        val extension = when {
            mimeType.contains(EXT_OGG) -> EXT_OGG
            mimeType.contains("mp4") || mimeType.contains(EXT_M4A) -> EXT_M4A
            mimeType.contains("mpeg") || mimeType.contains(EXT_MP3) -> EXT_MP3
            mimeType.contains(EXT_WAV) -> EXT_WAV
            else -> EXT_OGG
        }

        val response = client.post("${config.baseUrl}$TRANSCRIPTION_ENDPOINT") {
            header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
            if (config.referer.isNotBlank()) header(HEADER_HTTP_REFERER, config.referer)
            if (config.title.isNotBlank()) header(HEADER_X_TITLE, config.title)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(FORM_FIELD_MODEL, config.transcriptionModel)
                        append(
                            key = FORM_FIELD_FILE,
                            value = audioBytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, mimeType)
                                append(HttpHeaders.ContentDisposition, "filename=\"voice.$extension\"")
                            }
                        )
                    }
                )
            )
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.bodyAsText() }.getOrDefault("")
            logger.warn("Transcription failed: status={}, body={}", response.status, body)
            error("Transcription request failed with status ${response.status}: $body")
        }

        val result = response.body<TranscriptionResponse>()
        logger.info("Transcribed {} bytes -> {} chars", audioBytes.size, result.text.length)
        return result.text
    }

    companion object {
        const val DEFAULT_MIME_TYPE = "audio/ogg"
        private const val EXT_OGG = "ogg"
        private const val EXT_M4A = "m4a"
        private const val EXT_MP3 = "mp3"
        private const val EXT_WAV = "wav"
        private const val TRANSCRIPTION_ENDPOINT = "/audio/transcriptions"
        private const val HEADER_HTTP_REFERER = "HTTP-Referer"
        private const val HEADER_X_TITLE = "X-Title"
        private const val FORM_FIELD_MODEL = "model"
        private const val FORM_FIELD_FILE = "file"
    }
}

private data class TranscriptionResponse(val text: String)
