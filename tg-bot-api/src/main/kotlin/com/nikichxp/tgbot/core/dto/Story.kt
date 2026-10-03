package com.nikichxp.tgbot.core.dto

/**
 * Represents a message about a forwarded story in the chat.
 * https://core.telegram.org/bots/api#story
 */
data class Story(
    val chat: Chat,
    val id: Long
)
