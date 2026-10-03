package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.nikichxp.tgbot.core.dto.files.PhotoSize

/**
 * Contains information about a user that was shared with the bot using a KeyboardButtonRequestUsers button.
 * https://core.telegram.org/bots/api#shareduser
 */
data class SharedUser(
    @JsonProperty("user_id") val userId: Long,
    @JsonProperty("first_name") val firstName: String? = null,
    @JsonProperty("last_name") val lastName: String? = null,
    val username: String? = null,
    val photo: List<PhotoSize>? = null
)

/**
 * Contains information about the users whose identifiers were shared with the bot.
 * https://core.telegram.org/bots/api#usersshared
 */
data class UsersShared(
    @JsonProperty("request_id") val requestId: Int,
    val users: List<SharedUser>
)

/**
 * Contains information about the chat whose identifier was shared with the bot.
 * https://core.telegram.org/bots/api#chatshared
 */
data class ChatShared(
    @JsonProperty("request_id") val requestId: Int,
    @JsonProperty("chat_id") val chatId: Long,
    val title: String? = null,
    val username: String? = null,
    val photo: List<PhotoSize>? = null
)
