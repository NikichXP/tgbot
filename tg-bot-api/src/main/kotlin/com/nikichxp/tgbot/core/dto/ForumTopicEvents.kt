package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a service message about a new forum topic created in the chat.
 * https://core.telegram.org/bots/api#forumtopiccreated
 */
data class ForumTopicCreated(
    val name: String,
    @JsonProperty("icon_color") val iconColor: Int,
    @JsonProperty("icon_custom_emoji_id") val iconCustomEmojiId: String? = null
)

/**
 * Represents a service message about an edited forum topic.
 * https://core.telegram.org/bots/api#forumtopicedited
 */
data class ForumTopicEdited(
    val name: String? = null,
    @JsonProperty("icon_custom_emoji_id") val iconCustomEmojiId: String? = null
)

/**
 * Represents a service message about a forum topic closed in the chat.
 * https://core.telegram.org/bots/api#forumtopicclosed
 */
class ForumTopicClosed

/**
 * Represents a service message about a forum topic reopened in the chat.
 * https://core.telegram.org/bots/api#forumtopicreopened
 */
class ForumTopicReopened

/**
 * Represents a service message about General forum topic hidden in the chat.
 * https://core.telegram.org/bots/api#generalforumtopichidden
 */
class GeneralForumTopicHidden

/**
 * Represents a service message about General forum topic unhidden in the chat.
 * https://core.telegram.org/bots/api#generalforumtopicunhidden
 */
class GeneralForumTopicUnhidden
