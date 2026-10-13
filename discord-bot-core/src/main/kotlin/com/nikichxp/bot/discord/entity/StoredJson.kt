package com.nikichxp.bot.discord.entity

import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("storedJson")
data class StoredJson(
    val id: String,
    val json: String,
    val date: LocalDateTime
) {
    var additionalInfo: String? = null
}
