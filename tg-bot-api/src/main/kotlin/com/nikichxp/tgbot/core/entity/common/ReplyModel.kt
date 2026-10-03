package com.nikichxp.tgbot.core.entity.common

data class ReplyModel(
    val messageId: Long,
    val text: String?,
    val from: UserModel?,
    val chat: ChatModel
)
