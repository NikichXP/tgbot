package com.nikichxp.tgbot.core.dto.inlinequeryresults

import com.fasterxml.jackson.annotation.JsonProperty
import com.nikichxp.tgbot.core.dto.ParseMode

sealed class InputMessageContent {
    data class Text(
        @JsonProperty("message_text") val messageText: String,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("disable_web_page_preview") val disableWebPagePreview: Boolean? = null
    ) : InputMessageContent()

    data class Location(
        val latitude: Float,
        val longitude: Float,
        @JsonProperty("live_period") val livePeriod: Int? = null
    ) : InputMessageContent()

    data class Venue(
        val latitude: Float,
        val longitude: Float,
        val title: String,
        val address: String,
        @JsonProperty("foursquare_id") val foursquareId: String? = null,
        @JsonProperty("foursquare_type") val foursquareType: String? = null
    ) : InputMessageContent()

    data class Contact(
        @JsonProperty("phone_number") val phoneNumber: String,
        @JsonProperty("first_name") val firstName: String,
        @JsonProperty("last_name") val lastName: String? = null,
        val vcard: String? = null
    )
}
