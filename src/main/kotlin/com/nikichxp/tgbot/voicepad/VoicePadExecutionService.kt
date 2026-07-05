package com.nikichxp.tgbot.voicepad

import com.nikichxp.tgbot.core.entity.TgUpdateContext
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class VoicePadExecutionService(
    private val sessionService: VoicePadSessionService,
    private val fileDownloadService: TgFileDownloadService,
    private val transcriptionService: VoiceTranscriptionService,
    private val tgMessageService: TgMessageService,
    private val processors: List<VoicePadProcessor>
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun execute(session: VoicePadSession, chatId: Long, bot: TgBotInfo, updateContext: UpdateContext) {
        val processor = processors.find { it.command == session.command }
        if (processor == null) {
            logger.error("No processor found for command: {}", session.command)
            tgMessageService.sendMessage(chatId, MSG_UNKNOWN_COMMAND)
            return
        }

        tgMessageService.replyToCurrentMessage(
            MSG_PROCESSING.format(session.voiceEntries.size)
        )

        val triggerMessageId = session.triggerMessageId
        sessionService.completeSession(session)

        // TODO this is probably should be redesigned
        CoroutineScope(Dispatchers.IO + updateContext as TgUpdateContext).launch {
            try {
                val transcriptions = session.voiceEntries.map { entry ->
                    val bytes = fileDownloadService.downloadVoice(entry.fileId, bot)
                    transcriptionService.transcribe(bytes)
                }

                logger.info("Transcribed {} voices for session {}", transcriptions.size, session.id)

                val result = processor.process(transcriptions)
                val fileBytes = result.toByteArray(Charsets.UTF_8)

                tgMessageService.sendDocument(
                    chatId = chatId,
                    bot = bot,
                    fileName = processor.outputFileName,
                    fileContent = fileBytes,
                    replyToMessageId = triggerMessageId
                )
            } catch (e: Exception) {
                logger.error("VoicePad execute failed for session {}", session.id, e)
                tgMessageService.sendMessage(chatId, MSG_ERROR_PROCESSING.format(e.message ?: e.javaClass.simpleName))
            }
        }
    }

    suspend fun startSession(chatId: Long, userId: Long, messageId: Long, command: String, modeName: String): Boolean {
        var triggerMessageId: Long = messageId

        tgMessageService.sendMessage {
            sendInCurrentChat()
            text = MSG_SESSION_STARTED.format(modeName)
            withCallback { response ->
                triggerMessageId = response.result?.messageId ?: messageId
            }
        }

        sessionService.createSession(
            chatId = chatId,
            userId = userId,
            command = command,
            triggerMessageId = triggerMessageId
        )

        return true
    }

    companion object {
        private const val MSG_UNKNOWN_COMMAND = "Внутренняя ошибка: неизвестная команда сессии."
        private const val MSG_PROCESSING = "Обрабатываю %d войс(а/ов)... Это может занять минуту."
        private const val MSG_SESSION_STARTED = "Сессия создания %s запущена. Записывайте войсы реплаем на это сообщение. По окончанию отправьте /execute реплаем сюда же."
        private const val MSG_ERROR_PROCESSING = "Произошла ошибка при обработке: %s"
    }
}
