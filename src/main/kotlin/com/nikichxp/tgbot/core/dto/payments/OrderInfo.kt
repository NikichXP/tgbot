package com.nikichxp.tgbot.core.dto.payments

import com.fasterxml.jackson.annotation.JsonProperty as Name

/**
 *
 * This object represents information about an order.
 *
 * @property [name] User name
 * @property [phoneNumber] User's phone number
 * @property [email] User email
 * @property [shippingAddress] User shipping address
 * @see ShippingAddress
 */
data class OrderInfo(
    val name: String? = null,
    @Name("phone_number") val phoneNumber: String? = null,
    val email: String? = null,
    @Name("shipping_address") val shippingAddress: ShippingAddress? = null
)
