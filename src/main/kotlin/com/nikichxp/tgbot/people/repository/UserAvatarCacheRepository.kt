package com.nikichxp.tgbot.people.repository

import com.nikichxp.tgbot.people.dto.CachedAvatar
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.Base64

@Repository
class UserAvatarCacheRepository(
    private val redis: ReactiveStringRedisTemplate
) {

    suspend fun find(userId: Long): CachedAvatar? {
        val value = redis.opsForValue().get(key(userId)).awaitSingleOrNull() ?: return null
        return CachedAvatar(if (value == NO_AVATAR) null else Base64.getDecoder().decode(value))
    }

    suspend fun save(userId: Long, avatar: ByteArray?, ttl: Duration) {
        val value = avatar?.let { Base64.getEncoder().encodeToString(it) } ?: NO_AVATAR
        redis.opsForValue().set(key(userId), value, ttl).awaitSingle()
    }

    private fun key(userId: Long) = "people:avatar:$userId"

    companion object {
        private const val NO_AVATAR = "-"
    }
}
