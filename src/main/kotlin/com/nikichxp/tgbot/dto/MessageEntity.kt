package com.nikichxp.tgbot.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents one special entity in a text message. For example, hashtags, usernames, URLs, etc.
 */
data class MessageEntity(
    @JsonProperty("type") val type: Type,
    @JsonProperty("offset") val offset: Int,
    @JsonProperty("length") val length: Int,
    @JsonProperty("url") val url: String? = null,
    @JsonProperty("user") val user: User? = null,
    @JsonProperty("language") val language: String? = null
) {
    enum class Type {
        @JsonProperty("mention")
        MENTION,
        @JsonProperty("hashtag")
        HASHTAG,
        @JsonProperty("cashtag")
        CASHTAG,
        @JsonProperty("bot_command")
        BOT_COMMAND,
        @JsonProperty("url")
        URL,
        @JsonProperty("email")
        EMAIL,
        @JsonProperty("phone_number")
        PHONE_NUMBER,
        @JsonProperty("bold")
        BOLD,
        @JsonProperty("italic")
        ITALIC,
        @JsonProperty("underline")
        UNDERLINE,
        @JsonProperty("strikethrough")
        STRIKETHROUGH,
        @JsonProperty("code")
        CODE,
        @JsonProperty("pre")
        PRE,
        @JsonProperty("text_link")
        TEXT_LINK,
        @JsonProperty("text_mention")
        TEXT_MENTION
    }
}
