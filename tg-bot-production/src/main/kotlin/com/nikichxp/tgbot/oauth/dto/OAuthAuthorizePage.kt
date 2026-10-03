package com.nikichxp.tgbot.oauth.dto

data class OAuthAuthorizePage(
    val telegramClientId: String,
    val nonce: String,
    val clientName: String,
    val responseMode: OAuthResponseMode,
    val frameAncestors: List<String>
)
