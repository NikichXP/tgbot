package com.nikichxp.bot.common.entity.model

import com.nikichxp.bot.common.entity.UserId

data class UserModel(
    val id: UserId,
    val username: String?,
    val fullName: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val languageCode: String? = null,
    val isBot: Boolean? = null,
    val isPremium: Boolean? = null
)
