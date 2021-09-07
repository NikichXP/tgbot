package com.nikichxp.tgbot.dto.keyboard

import com.nikichxp.tgbot.dto.polls.PollType
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents one button of the reply keyboard.
 * For simple text buttons String can be used instead of this object to specify text of the button.
 * Optional fields requestContact, requestLocation, and requestPoll are mutually exclusive.
 * https://core.telegram.org/bots/api#keyboardbutton
 */
data class KeyboardButton(
    @JsonProperty(KeyboardFields.TEXT) val text: String,
    @JsonProperty(KeyboardFields.REQUEST_CONTACT) val requestContact: Boolean? = null,
    @JsonProperty(KeyboardFields.REQUEST_LOCATION) val requestLocation: Boolean? = null,
    @JsonProperty(KeyboardFields.REQUEST_POLL) val requestPoll: KeyboardButtonPollType? = null
)

/**
 * Represents type of a poll, which is allowed to be created and sent when the corresponding button is pressed.
 * https://core.telegram.org/bots/api#keyboardbuttonpolltype
 */
data class KeyboardButtonPollType(
    @JsonProperty(KeyboardFields.TYPE) val type: PollType? = null
)
