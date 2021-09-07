package com.nikichxp.tgbot.dto

import com.fasterxml.jackson.annotation.JsonProperty as Name

data class Invoice(
    val title: String,
    val description: String,
    @Name("start_parameter") val startParameter: String,
    val currency: String,
    @Name("total_amount") val totalAmount: Int
)
