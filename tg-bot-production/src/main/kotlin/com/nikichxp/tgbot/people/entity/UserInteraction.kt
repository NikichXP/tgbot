package com.nikichxp.tgbot.people.entity

import org.springframework.data.mongodb.core.mapping.Document

@Document("userInteraction")
data class UserInteraction(
    val userId: Long,
    val botName: String
)
