package com.nikichxp.tgbot.voicepad

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.UUID

@Document(collection = VoicePadSession.COLLECTION_NAME)
data class VoicePadSession(
    val chatId: Long,
    val userId: Long,
    val command: String,
    val triggerMessageId: Long,
    val voiceEntries: MutableList<VoiceEntry> = mutableListOf(),
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    @Id
    var id: String = UUID.randomUUID().toString()

    var status: SessionStatus = SessionStatus.ACTIVE

    companion object {
        const val COLLECTION_NAME = "voicePadSessions"
    }
}

enum class SessionStatus { ACTIVE, COMPLETED }

data class VoiceEntry(
    val messageId: Long,
    val fileId: String,
    val fileUniqueId: String
)
