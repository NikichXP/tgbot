package com.nikichxp.tgbot.dto.payments

data class ShippingOption(
    val id: String,
    val title: String,
    val prices: List<LabeledPrice>
)
