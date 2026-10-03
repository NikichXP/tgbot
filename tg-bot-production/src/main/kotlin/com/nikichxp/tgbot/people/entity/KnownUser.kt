package com.nikichxp.tgbot.people.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "knownUsers")
data class KnownUser(
    @Id val id: Long,
    @Indexed val username: String?,
    val fullName: String,
    val firstName: String?,
    val lastName: String?,
    val languageCode: String?,
    val isBot: Boolean?,
    val isPremium: Boolean?,
    val firstSeenAt: Instant,
    @Indexed val lastSeenAt: Instant,
    val bots: Set<String> = emptySet(),
    val chats: Map<String, KnownUserChat> = emptyMap()
)

data class KnownUserChat(
    val chatId: Long,
    val title: String,
    val type: String,
    val lastSeenAt: Instant
)
