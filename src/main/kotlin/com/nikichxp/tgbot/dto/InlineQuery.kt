package com.nikichxp.tgbot.dto

data class InlineQuery(
    val id: String,
    val from: User,
    val location: Location? = null,
    val query: String,
    val offset: String
)
