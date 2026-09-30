package com.nikichxp.tgbot.dashboard.repository

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class DashboardLoginNonceRepository(
    private val redis: ReactiveStringRedisTemplate
) {

    suspend fun save(nonce: String, ttl: Duration) {
        redis.opsForValue().set(key(nonce), "1", ttl).awaitSingle()
    }

    suspend fun consumeIfPresent(nonce: String): Boolean = redis.delete(key(nonce)).awaitSingle() > 0

    private fun key(nonce: String) = "dashboard:nonce:$nonce"
}
