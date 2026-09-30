package com.nikichxp.tgbot.oauth.dto

import java.time.Instant

data class OAuthAuthorizationGrant(
    val clientId: String,
    val redirectUri: String,
    val user: OAuthUserDto,
    val authTime: Instant
)
