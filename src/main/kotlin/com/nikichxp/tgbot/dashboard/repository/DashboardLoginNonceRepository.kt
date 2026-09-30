package com.nikichxp.tgbot.dashboard.repository

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

/** One-time nonces for Telegram login: issued before the popup opens, consumed when the id_token arrives. */
@Repository
class DashboardLoginNonceRepository(private val redis: ReactiveStringRedisTemplate) {

    suspend fun save(nonce: String, ttl: Duration) {
        redis.opsForValue().set(key(nonce), "1", ttl).awaitSingle()
    }

    /** @return true if the nonce existed (and is now gone) */
    suspend fun consume(nonce: String): Boolean = redis.delete(key(nonce)).awaitSingle() > 0

    private fun key(nonce: String) = "dashboard:nonce:$nonce"
}
