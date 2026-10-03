package com.nikichxp.tgbot.oauth.dto

data class OAuthLoginCompletion(
    val responseMode: OAuthResponseMode,
    val redirectUri: String,
    val targetOrigin: String,
    val code: String,
    val state: String?
)
