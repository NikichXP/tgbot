package com.nikichxp.tgbot.voicepad

import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
class VoicePadSessionService(
    private val mongoTemplate: MongoTemplate
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private fun findByChatIdAndStatus(chatId: Long, status: SessionStatus): VoicePadSession? {
        val query = Query(Criteria.where("chatId").`is`(chatId).and("status").`is`(status))
        return mongoTemplate.findOne<VoicePadSession>(query)
    }

    fun createSession(chatId: Long, userId: Long, command: String, triggerMessageId: Long): VoicePadSession {
        val existing = findByChatIdAndStatus(chatId, SessionStatus.ACTIVE)
        if (existing != null) {
            existing.status = SessionStatus.COMPLETED
            mongoTemplate.save(existing)
            logger.info("Closed existing session {} for chat {}", existing.id, chatId)
        }

        val session = VoicePadSession(
            chatId = chatId,
            userId = userId,
            command = command,
            triggerMessageId = triggerMessageId
        )
        return mongoTemplate.save(session).also {
            logger.info("Created session {} command={} chat={}", it.id, command, chatId)
        }
    }

    fun getActiveSession(chatId: Long): VoicePadSession? =
        findByChatIdAndStatus(chatId, SessionStatus.ACTIVE)

    fun addVoice(session: VoicePadSession, entry: VoiceEntry): VoicePadSession {
        session.voiceEntries.add(entry)
        return mongoTemplate.save(session)
    }

    fun removeVoice(session: VoicePadSession, messageId: Long): Boolean {
        val removed = session.voiceEntries.removeIf { it.messageId == messageId }
        if (removed) mongoTemplate.save(session)
        return removed
    }

    fun completeSession(session: VoicePadSession): VoicePadSession {
        session.status = SessionStatus.COMPLETED
        return mongoTemplate.save(session).also {
            logger.info("Completed session {} chat={}", it.id, it.chatId)
        }
    }
}
