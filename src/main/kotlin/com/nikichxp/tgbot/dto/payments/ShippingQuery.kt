package com.nikichxp.tgbot.dto.payments

import com.nikichxp.tgbot.dto.User
import com.fasterxml.jackson.annotation.JsonProperty

data class ShippingQuery(
    val id: String,
    val from: User,
    @JsonProperty("invoice_payload") val invoicePayload: String,
    @JsonProperty("shipping_address") val shippingAddress: ShippingAddress
)
