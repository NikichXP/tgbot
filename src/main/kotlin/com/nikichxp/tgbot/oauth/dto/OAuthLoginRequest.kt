package com.nikichxp.tgbot.oauth.dto

data class OAuthLoginRequest(
    val clientId: String,
    val redirectUri: String,
    val state: String?,
    val responseMode: OAuthResponseMode
)
