package com.nikichxp.tgbot.dashboard.dto

/** Verified data of a Telegram Login Widget user. */
data class TelegramLoginUser(
    val id: Long,
    val firstName: String?,
    val lastName: String?,
    val username: String?,
    val photoUrl: String?
)
