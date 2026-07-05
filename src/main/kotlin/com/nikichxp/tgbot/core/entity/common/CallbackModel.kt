package com.nikichxp.tgbot.core.entity.common

data class CallbackModel(
    val userId: Long,
    val data: String,
    val messageText: String?,
    val buttonText: String,
    val chatId: Long,
    val messageId: Long
)
