package com.nikichxp.tgbot.entity

data class MessageInteractionResult(
    val userId: String,
    val interaction: Double = .0,
    val target: String? = null
)