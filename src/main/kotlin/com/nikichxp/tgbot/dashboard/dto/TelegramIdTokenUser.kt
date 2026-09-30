package com.nikichxp.tgbot.dashboard.dto

/** User claims of a verified Telegram OIDC `id_token`. */
data class TelegramIdTokenUser(
    val id: Long,
    val name: String?,
    val username: String?,
    val photoUrl: String?,
    val nonce: String?
)
