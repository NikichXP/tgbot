package com.nikichxp.tgbot.core.entity.common

import com.nikichxp.tgbot.core.entity.UserId

data class CallbackModel(
    val userId: UserId,
    val data: String,
    val messageText: String?,
    val buttonText: String?,
    val chatId: Long,
    val messageId: Long
)
