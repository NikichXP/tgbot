package com.nikichxp.tgbot.dashboard.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "dashboardSessions")
data class DashboardSession(
    @Id val refreshTokenHash: String,
    val userId: Long,
    val name: String?,
    val username: String?,
    val photoUrl: String?,
    val userAgent: String?,
    val createdAt: Instant,
    var lastUsedAt: Instant
)
