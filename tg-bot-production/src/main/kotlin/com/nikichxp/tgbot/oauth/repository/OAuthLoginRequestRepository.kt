package com.nikichxp.tgbot.oauth.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nikichxp.tgbot.oauth.dto.OAuthLoginRequest
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class OAuthLoginRequestRepository(
    private val redis: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper
) {

    suspend fun save(nonce: String, request: OAuthLoginRequest, ttl: Duration) {
        redis.opsForValue().set(key(nonce), objectMapper.writeValueAsString(request), ttl).awaitSingle()
    }

    suspend fun consume(nonce: String): OAuthLoginRequest? =
        redis.opsForValue().getAndDelete(key(nonce)).awaitSingleOrNull()?.let { objectMapper.readValue(it) }

    private fun key(nonce: String) = "oauth:request:$nonce"
}
