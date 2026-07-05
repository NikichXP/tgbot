package com.nikichxp.tgbot.voicepad

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgUpdateContext
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.core.util.getContextMessageId
import com.nikichxp.tgbot.core.util.getContextUserId
import kotlinx.coroutines.currentCoroutineContext
import org.springframework.stereotype.Service

@Service
class VoicePadCommandHandler(
    private val sessionService: VoicePadSessionService,
    private val executionService: VoicePadExecutionService,
    private val tgMessageService: TgMessageService,
    private val appConfig: AppConfig
) : CommandHandler, UpdateHandler, Authenticable {

    override suspend fun authenticate(update: Update): Boolean =
        update.getContextUserId() == appConfig.adminId

    override fun requiredFeatures() = setOf(Features.TOOLBOX)

    // UpdateHandler: fires for voice messages that are replies (to capture voices added to a session)
    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.HAS_VOICE, UpdateMarker.REPLY)

    override fun canHandle(update: Update): Boolean {
        val chatId = update.getContextChatId() ?: return false
        val replyToId = update.message?.replyToMessage?.messageId ?: return false
        val session = sessionService.getActiveSession(chatId) ?: return false
        return session.triggerMessageId == replyToId
    }

    override suspend fun handleUpdate(update: Update) {
        val chatId = update.getContextChatId() ?: return
        val voice = update.message?.voice ?: return
        val messageId = update.getContextMessageId() ?: return

        val session = sessionService.getActiveSession(chatId) ?: return

        val entry = VoiceEntry(
            messageId = messageId,
            fileId = voice.fileId,
            fileUniqueId = voice.fileUniqueId
        )
        val updated = sessionService.addVoice(session, entry)
        val count = updated.voiceEntries.size

        tgMessageService.replyToCurrentMessage(MSG_VOICE_ADDED.format(count))
    }

    @HandleCommand(CMD_CREATE_PROMPT)
    suspend fun createPrompt(update: Update): Boolean {
        val chatId = update.getContextChatId() ?: return false
        val userId = update.getContextUserId() ?: return false
        val messageId = update.getContextMessageId() ?: return false
        return executionService.startSession(chatId, userId, messageId, CMD_CREATE_PROMPT, MODE_PROMPT)
    }

    @HandleCommand(CMD_CREATE_NOTEPAD)
    suspend fun createNotepad(update: Update): Boolean {
        val chatId = update.getContextChatId() ?: return false
        val userId = update.getContextUserId() ?: return false
        val messageId = update.getContextMessageId() ?: return false
        return executionService.startSession(chatId, userId, messageId, CMD_CREATE_NOTEPAD, MODE_NOTEPAD)
    }

    @HandleCommand("/execute")
    suspend fun execute(updateContext: UpdateContext): Boolean {
        val chatId = updateContext.getChatId()
        val session = sessionService.getActiveSession(chatId)

        if (session == null) {
            tgMessageService.replyToCurrentMessage(MSG_NO_ACTIVE_SESSION)
            return false
        }

        if (session.voiceEntries.isEmpty()) {
            tgMessageService.replyToCurrentMessage(MSG_NO_VOICES)
            return false
        }

        val updateContext = currentCoroutineContext()[TgUpdateContext] ?: throw IllegalStateException("No update context")
        executionService.execute(session, chatId, updateContext.tgBotV2, updateContext)
        return true
    }

    @HandleCommand("/delete")
    suspend fun deleteVoice(update: Update): Boolean {
        val chatId = update.getContextChatId() ?: return false
        val replyToMessage = update.message?.replyToMessage

        if (replyToMessage == null) {
            tgMessageService.replyToCurrentMessage(MSG_DELETE_NO_REPLY)
            return false
        }

        val session = sessionService.getActiveSession(chatId)

        if (session == null) {
            tgMessageService.replyToCurrentMessage(MSG_NO_SESSION)
            return false
        }

        val targetMessageId = replyToMessage.messageId
        val removed = sessionService.removeVoice(session, targetMessageId)

        return if (removed) {
            val remaining = sessionService.getActiveSession(chatId)?.voiceEntries?.size ?: 0
            tgMessageService.replyToCurrentMessage(MSG_VOICE_REMOVED.format(remaining))
            true
        } else {
            tgMessageService.replyToCurrentMessage(MSG_NOT_IN_SESSION)
            false
        }
    }

    companion object {
        const val CMD_CREATE_PROMPT = "/create-prompt"
        const val CMD_CREATE_NOTEPAD = "/create-notepad"
        private const val MODE_PROMPT = "промпта/плана реализации"
        private const val MODE_NOTEPAD = "конспекта"
        private const val MSG_VOICE_ADDED = "Войс добавлен (%d всего). Запишите следующий или отправьте /execute реплаем сюда."
        private const val MSG_NO_ACTIVE_SESSION = "Нет активной сессии. Начните с /create-prompt или /create-notepad."
        private const val MSG_NO_VOICES = "Нет войсов для обработки. Запишите хотя бы один голосовой фрагмент реплаем на стартовое сообщение."
        private const val MSG_DELETE_NO_REPLY = "Отправьте /delete реплаем на голосовое сообщение, которое хотите удалить из сессии."
        private const val MSG_NO_SESSION = "Нет активной сессии."
        private const val MSG_VOICE_REMOVED = "Войс удалён из сессии. Осталось войсов: %d."
        private const val MSG_NOT_IN_SESSION = "Это сообщение не найдено в текущей сессии."
    }
}
