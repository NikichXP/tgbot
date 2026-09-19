package com.nikichxp.tgbot.core.entity

import com.nikichxp.tgbot.core.entity.common.UserModel

data class MessageInteractionResult(
    val users: MutableMap<UserModel, InteractionRole>,
    val interactionType: InteractionType,
    val power: Double = .0
) {

    constructor(
        users: MutableMap<UserModel, InteractionRole>,
        power: Double = .0
    ) : this(
        users = users, power = power,
        interactionType = if (power == 0.0) InteractionType.NONE else InteractionType.RATING
    )

    fun getActor(): UserModel = users.filterValues { it == InteractionRole.ACTOR }.keys.first()

    fun getTarget(): UserModel? = users.filterValues { it == InteractionRole.TARGET }.keys.firstOrNull()

    fun isNoInteraction(): Boolean = getTarget() == null
    fun isLikeInteraction(): Boolean = getTarget() != null && power != 0.0

}

enum class InteractionRole {
    ACTOR, TARGET
}

enum class InteractionType {
    NONE, RATING // TODO ban, etc in the future
}