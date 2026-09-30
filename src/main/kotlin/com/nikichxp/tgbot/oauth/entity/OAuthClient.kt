package com.nikichxp.tgbot.oauth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "oauthClients")
data class OAuthClient(
    @Id val clientId: String,
    val name: String,
    val secretHash: String,
    val redirectUris: List<String>,
    val createdAt: Instant
)
