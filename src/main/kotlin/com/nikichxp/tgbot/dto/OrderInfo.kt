package com.nikichxp.tgbot.dto

import com.fasterxml.jackson.annotation.JsonProperty as Name

data class OrderInfo(
    val name: String? = null,
    @Name("phone_number") val phoneNumber: String? = null,
    val email: String? = null,
    @Name("shipping_address") val shippingAddress: ShippingAddress? = null
)
