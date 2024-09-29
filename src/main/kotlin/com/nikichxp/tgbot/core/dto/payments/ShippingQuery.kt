package com.nikichxp.tgbot.core.dto.payments

import com.fasterxml.jackson.annotation.JsonProperty
import com.nikichxp.tgbot.core.dto.User

data class ShippingQuery(
    val id: String,
    val from: User,
    @JsonProperty("invoice_payload") val invoicePayload: String,
    @JsonProperty("shipping_address") val shippingAddress: ShippingAddress
)
