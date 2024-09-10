package com.nikichxp.tgbot.core.dto.payments

data class ShippingOption(
    val id: String,
    val title: String,
    val prices: List<LabeledPrice>
)
