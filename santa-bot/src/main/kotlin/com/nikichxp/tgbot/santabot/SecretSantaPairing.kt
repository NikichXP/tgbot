package com.nikichxp.tgbot.santabot

import com.nikichxp.tgbot.santabot.entity.SecretSantaPlayer
import java.util.*

object SecretSantaPairing {

    fun findPairs(
        players: List<SecretSantaPlayer>,
        random: Random = Random()
    ): List<Pair<SecretSantaPlayer, SecretSantaPlayer>>? {
        val targets = players.toMutableList()
        val iterationLimit = players.size * players.size * 2
        var iteration = 0

        while (!arePairsValid(players.zip(targets))) {
            if (iteration++ >= iterationLimit) return null
            Collections.swap(targets, random.nextInt(players.size), random.nextInt(players.size))
        }
        return players.zip(targets)
    }

    fun arePairsValid(pairs: List<Pair<SecretSantaPlayer, SecretSantaPlayer>>): Boolean {
        val targetIdBySantaId = pairs.associate { (santa, target) -> santa.id to target.id }
        return pairs.all { (santa, target) ->
            santa.id != target.id &&
                    !santa.ignores(target) &&
                    !target.ignores(santa) &&
                    targetIdBySantaId[target.id] != santa.id
        }
    }

    private fun SecretSantaPlayer.ignores(other: SecretSantaPlayer): Boolean =
        other.user.username?.lowercase() in ignoredUsernames
}
