package com.nikichxp.tgbot.dashboard.dto

data class AuthConfigResponse(val clientId: String)

data class NonceResponse(val nonce: String)

data class LoginResponse(val accessToken: String, val expiresIn: Long, val user: DashboardUserDto)

data class RefreshResponse(val accessToken: String, val expiresIn: Long)

data class ErrorResponse(val error: String)
