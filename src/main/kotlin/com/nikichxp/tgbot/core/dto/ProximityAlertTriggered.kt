package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents the content of a service message about a user in the chat triggering a proximity alert.
 * https://core.telegram.org/bots/api#proximityalerttriggered
 */
data class ProximityAlertTriggered(
    val traveler: User,
    val watcher: User,
    val distance: Int
)

/**
 * Represents a service message about a change in auto-delete timer settings.
 * https://core.telegram.org/bots/api#messageautodeletetimerchanged
 */
data class MessageAutoDeleteTimerChanged(
    @JsonProperty("message_auto_delete_time") val messageAutoDeleteTime: Int
)
