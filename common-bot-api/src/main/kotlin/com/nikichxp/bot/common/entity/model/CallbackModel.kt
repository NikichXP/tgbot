package com.nikichxp.bot.common.entity.model

import com.nikichxp.bot.common.entity.UserId

data class CallbackModel(
    val userId: UserId,
    val data: String,
    val messageText: String?,
    val buttonText: String?,
    val chatId: Long,
    val messageId: Long
)
