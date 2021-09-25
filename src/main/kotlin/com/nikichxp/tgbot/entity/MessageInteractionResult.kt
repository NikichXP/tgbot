package com.nikichxp.tgbot.entity

data class MessageInteractionResult(
    val users: MutableMap<Long, InteractionRole>,
    val type: InteractionType,
    val power: Double = .0
) {

    constructor(
        users: MutableMap<Long, InteractionRole>,
        power: Double = .0
    ) : this(
        users = users, power = power,
        type = if (power == 0.0) InteractionType.NONE else InteractionType.RATING
    )

    companion object {
        fun emptyFrom(userId: Long): MessageInteractionResult {
            return MessageInteractionResult(
                mutableMapOf(userId to InteractionRole.LIKED),
                .0
            )
        }
    }

}

enum class InteractionRole {
    LIKED, TARGET
}

enum class InteractionType {
    RATING, NONE // TODO ban, etc in the future
}