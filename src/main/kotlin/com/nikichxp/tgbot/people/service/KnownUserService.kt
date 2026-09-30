package com.nikichxp.tgbot.people.service

import com.nikichxp.tgbot.people.dto.SeenUser
import com.nikichxp.tgbot.people.entity.KnownUser
import com.nikichxp.tgbot.people.repository.KnownUserRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class KnownUserService(private val knownUserRepository: KnownUserRepository) {

    private val sameSightingThrottle = WriteThrottle(Duration.ofMinutes(10))

    suspend fun recordSeen(seenUsers: List<SeenUser>) {
        seenUsers
            .filter { it.user.isBot != true }
            .distinctBy { it.user.id }
            .filter { sameSightingThrottle.tryAcquire(sightingKey(it)) }
            .forEach { knownUserRepository.upsertSeen(it, Instant.now()) }
    }

    suspend fun search(text: String?, limit: Int): List<KnownUser> = knownUserRepository.search(text, limit)

    suspend fun findAllById(ids: Collection<Long>): Map<Long, KnownUser> =
        if (ids.isEmpty()) emptyMap() else knownUserRepository.findAllById(ids).associateBy { it.id }

    private fun sightingKey(seen: SeenUser) = "${seen.user.hashCode()}:${seen.botName}:${seen.chat?.hashCode()}"
}
