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

    companion object {
        fun emptyFrom(user: User): MessageInteractionResult {
            return MessageInteractionResult(
                mutableMapOf(user to InteractionRole.NONE),
                .0
            )
        }
    }

}

enum class InteractionRole {
    NONE, LIKED, TARGET
}

enum class InteractionType {
    NONE, RATING // TODO ban, etc in the future
}