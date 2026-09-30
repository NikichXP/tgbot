package com.nikichxp.tgbot.dashboard.dto

data class TelegramIdTokenUser(
    val id: Long,
    val name: String?,
    val username: String?,
    val photoUrl: String?,
    val nonce: String?
)
