package com.nikichxp.tgbot.people.service

import com.nikichxp.tgbot.core.service.TgBotV2Service
import com.nikichxp.tgbot.people.connector.TelegramProfilePhotoConnector
import com.nikichxp.tgbot.people.repository.KnownUserRepository
import com.nikichxp.tgbot.people.repository.UserAvatarCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class UserAvatarService(
    private val knownUserRepository: KnownUserRepository,
    private val tgBotV2Service: TgBotV2Service,
    private val profilePhotoConnector: TelegramProfilePhotoConnector,
    private val avatarCache: UserAvatarCacheRepository
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getAvatar(userId: Long): ByteArray? {
        avatarCache.find(userId)?.let { return it.bytes }
        val avatar = downloadViaAnyBotThatSawUser(userId)
        avatarCache.save(userId, avatar, if (avatar != null) AVATAR_TTL else NO_AVATAR_TTL)
        return avatar
    }

    private suspend fun downloadViaAnyBotThatSawUser(userId: Long): ByteArray? {
        val botNames = knownUserRepository.findAllById(listOf(userId)).firstOrNull()?.bots.orEmpty()
        for (botName in botNames) {
            val bot = runCatching { withContext(Dispatchers.IO) { tgBotV2Service.getBotById(botName) } }.getOrNull()
                ?: continue
            val photo = runCatching { profilePhotoConnector.downloadCurrentProfilePhoto(bot, userId) }
                .onFailure { logger.warn("Failed to get avatar of user $userId via bot $botName: ${it.message}") }
                .getOrNull()
            if (photo != null) return photo
        }
        return null
    }

    companion object {
        private val AVATAR_TTL = Duration.ofHours(12)
        private val NO_AVATAR_TTL = Duration.ofHours(1)
    }
}
