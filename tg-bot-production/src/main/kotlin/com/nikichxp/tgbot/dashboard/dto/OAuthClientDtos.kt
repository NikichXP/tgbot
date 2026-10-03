package com.nikichxp.tgbot.dashboard.dto

import java.time.Instant

data class OAuthClientDto(
    val clientId: String,
    val name: String,
    val redirectUris: List<String>,
    val createdAt: Instant
)

data class CreateOAuthClientRequest(
    val clientId: String,
    val name: String? = null,
    val redirectUris: List<String>
)

data class OAuthClientWithSecretDto(val client: OAuthClientDto, val clientSecret: String)
