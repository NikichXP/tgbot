package com.nikichxp.tgbot.dashboard.repository

import com.nikichxp.tgbot.dashboard.dto.DashboardPrincipal
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class DashboardAccessTokenRepository(
    private val redis: ReactiveStringRedisTemplate
) {

    suspend fun save(tokenHash: String, principal: DashboardPrincipal, ttl: Duration) {
        redis.opsForValue().set(key(tokenHash), "${principal.userId}:${principal.sessionId}", ttl).awaitSingle()
    }

    suspend fun find(tokenHash: String): DashboardPrincipal? {
        val value = redis.opsForValue().get(key(tokenHash)).awaitSingleOrNull() ?: return null
        val (userId, sessionId) = value.split(":", limit = 2)
        return DashboardPrincipal(userId.toLong(), sessionId)
    }

    suspend fun delete(tokenHash: String) {
        redis.delete(key(tokenHash)).awaitSingle()
    }

    private fun key(tokenHash: String) = "dashboard:access:$tokenHash"
}
