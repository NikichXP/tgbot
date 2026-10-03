package com.nikichxp.tgbot.oauth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class OAuthPageLoginRequest(val idToken: String)

data class OAuthTokenRequest(
    val grantType: String?,
    val code: String?,
    val redirectUri: String?,
    val clientId: String?,
    val clientSecret: String?
)

data class OAuthTokenResponse(val user: OAuthUserDto, val authTime: Long)

data class OAuthErrorResponse(
    val error: String,
    @get:JsonProperty("error_description") val errorDescription: String
)
