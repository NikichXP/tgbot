package com.nikichxp.tgbot.core.dto

/**
 * Represents a boost added to a chat.
 * https://core.telegram.org/bots/api#chatboostupdated
 */
data class ChatBoostUpdated(
    val chat: Chat,
    val boost: ChatBoost
)
