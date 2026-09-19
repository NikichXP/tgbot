package com.nikichxp.tgbot.core.entity.common

import com.nikichxp.tgbot.core.entity.UserId

data class UserModel(
    val id: UserId,
    val username: String?,
    val fullName: String
)
