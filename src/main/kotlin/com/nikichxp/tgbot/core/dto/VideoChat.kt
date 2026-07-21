package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a service message about a video chat scheduled in the chat.
 * https://core.telegram.org/bots/api#videochatscheduled
 */
data class VideoChatScheduled(
    @JsonProperty("start_date") val startDate: Long
)

/**
 * Represents a service message about a video chat started in the chat.
 * https://core.telegram.org/bots/api#videochatstarted
 */
class VideoChatStarted

/**
 * Represents a service message about a video chat ended in the chat.
 * https://core.telegram.org/bots/api#videochatended
 */
data class VideoChatEnded(
    val duration: Int
)

/**
 * Represents a service message about new members invited to a video chat.
 * https://core.telegram.org/bots/api#videochatparticipantsinvited
 */
data class VideoChatParticipantsInvited(
    val users: List<User>
)
