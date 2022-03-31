package com.nikichxp.tgbot.entity

import com.nikichxp.tgbot.dto.User

data class MessageInteractionResult(
    val users: MutableMap<User, InteractionRole>,
    val interactionType: InteractionType,
    val power: Double = .0
) {

    constructor(
        users: MutableMap<User, InteractionRole>,
        power: Double = .0
    ) : this(
        users = users, power = power,
        interactionType = if (power == 0.0) InteractionType.NONE else InteractionType.RATING
    )

    fun getActor(): User = users.filterValues { it == InteractionRole.ACTOR }.keys.first()

    fun getTarget(): User? = users.filterValues { it == InteractionRole.TARGET }.keys.firstOrNull()

    fun isNoInteraction(): Boolean = getTarget() == null
    fun isLikeInteraction(): Boolean = getTarget() != null && power != 0.0

}

enum class InteractionRole {
    ACTOR, TARGET
}

enum class InteractionType {
    NONE, RATING // TODO ban, etc in the future
}