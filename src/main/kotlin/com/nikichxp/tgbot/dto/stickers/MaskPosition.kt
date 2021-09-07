package com.nikichxp.tgbot.dto.stickers

import com.fasterxml.jackson.annotation.JsonProperty

data class MaskPosition(
    val point: String,
    @JsonProperty("x_shift") val xShift: Float,
    @JsonProperty("y_shift") val yShift: Float,
    val scale: Float
)
