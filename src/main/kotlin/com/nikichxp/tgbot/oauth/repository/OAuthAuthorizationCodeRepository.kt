package com.nikichxp.tgbot.oauth.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nikichxp.tgbot.oauth.dto.OAuthAuthorizationGrant
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class OAuthAuthorizationCodeRepository(
    private val redis: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper
) {

    suspend fun save(codeHash: String, grant: OAuthAuthorizationGrant, ttl: Duration) {
        redis.opsForValue().set(key(codeHash), objectMapper.writeValueAsString(grant), ttl).awaitSingle()
    }

    suspend fun consume(codeHash: String): OAuthAuthorizationGrant? =
        redis.opsForValue().getAndDelete(key(codeHash)).awaitSingleOrNull()?.let { objectMapper.readValue(it) }

    private fun key(codeHash: String) = "oauth:code:$codeHash"
}
