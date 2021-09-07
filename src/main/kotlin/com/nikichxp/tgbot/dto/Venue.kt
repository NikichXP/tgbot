package com.nikichxp.tgbot.dto

import com.fasterxml.jackson.annotation.JsonProperty as Name

data class Venue(
    val location: Location,
    val title: String,
    val address: String,
    @Name("foursquare_id") val foursquareId: String? = null,
    @Name("foursquare_type") val foursquareType: String? = null
)
