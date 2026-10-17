package com.nikichxp.tgbot.santabot.entity

import com.nikichxp.bot.common.entity.model.UserModel
import java.util.*

class SecretSantaGame {
    var id: String = UUID.randomUUID().toString().substring(0 until 8)
    var players: List<SecretSantaPlayer> = listOf()
    var isStarted = false
    var createdBy: Long = 0
}

data class SecretSantaPlayer(
    val user: UserModel
) {
    val id: Long get() = user.id
    var ignoredUsernames = setOf<String>()
}
