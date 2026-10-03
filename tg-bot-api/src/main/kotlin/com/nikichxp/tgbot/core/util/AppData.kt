package com.nikichxp.tgbot.core.util

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("app_data")
data class AppData(
    @Id val key: String,
    val value: String
)
