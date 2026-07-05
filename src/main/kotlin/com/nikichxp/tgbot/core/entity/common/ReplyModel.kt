package com.nikichxp.tgbot.core.entity.common

data class ReplyModel(
    val id: String,
    val username: String?,
    val fullName: String,
    val messageId: String,
    val text: String,
    val chatId: String,
    val chatType: String,
    val chatTitle: String
)
