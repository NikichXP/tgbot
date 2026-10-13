package com.nikichxp.bot.common.entity.model

data class ReplyModel(
    val messageId: Long,
    val text: String?,
    val from: UserModel?,
    val chat: ChatModel
)
