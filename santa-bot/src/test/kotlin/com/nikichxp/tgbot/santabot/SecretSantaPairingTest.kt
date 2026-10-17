package com.nikichxp.tgbot.santabot

import com.nikichxp.bot.common.entity.model.UserModel
import com.nikichxp.tgbot.santabot.entity.SecretSantaPlayer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class SecretSantaPairingTest {

    private fun player(id: Long, username: String?, vararg ignores: String) =
        SecretSantaPlayer(UserModel(id = id, username = username, fullName = "User $id"))
            .apply { ignoredUsernames = ignores.toSet() }

    @Test
    fun foundPairsAreValidForEveryone() {
        val players = listOf(player(1, "a"), player(2, null), player(3, "c"), player(4, "d", "a"))

        val pairs = SecretSantaPairing.findPairs(players, Random(42))

        assertThat(pairs).isNotNull
        assertThat(pairs!!.map { it.first }).containsExactlyElementsOf(players)
        assertThat(pairs.map { it.second.id }).containsExactlyInAnyOrder(1, 2, 3, 4)
        assertThat(SecretSantaPairing.arePairsValid(pairs)).isTrue()
    }

    @Test
    fun rejectsSelfMutualAndIgnoredPairs() {
        val a = player(1, "a")
        val b = player(2, "b", "c")
        val c = player(3, "c")

        assertThat(SecretSantaPairing.arePairsValid(listOf(a to a))).isFalse()
        assertThat(SecretSantaPairing.arePairsValid(listOf(a to b, b to a))).isFalse()
        assertThat(SecretSantaPairing.arePairsValid(listOf(a to b, b to c, c to a))).isFalse()
        assertThat(SecretSantaPairing.arePairsValid(listOf(a to c, c to b, b to a))).isFalse()
        assertThat(SecretSantaPairing.arePairsValid(listOf(a to c, c to player(2, "b"), player(2, "b") to a))).isTrue()
    }

    @Test
    fun returnsNullWhenNoValidPairingExists() {
        assertThat(SecretSantaPairing.findPairs(listOf(player(1, "a"), player(2, "b")), Random(1))).isNull()
    }
}
