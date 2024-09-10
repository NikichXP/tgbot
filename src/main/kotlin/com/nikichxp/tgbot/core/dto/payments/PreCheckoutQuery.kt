package com.nikichxp.tgbot.core.dto.payments

import com.nikichxp.tgbot.core.dto.User
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigInteger

/**
 *
 * This object contains information about an incoming pre-checkout query.
 *
 * @property [id] Unique query identifier
 * @property [from] User who sent the query
 * @property [currency] Three-letter ISO 4217 currency code
 * @property [totalAmount] Total price in the smallest units of the currency (integer, not float/double). For example, for a price of US$ 1.45 pass amount = 145.
 * @property [invoicePayload] Bot specified invoice payload
 * @property [shippingOptionId] Identifier of the shipping option chosen by the user
 * @property [orderInfo] Order info provided by the user
 * @see OrderInfo
 */
data class PreCheckoutQuery(
    val id: String,
    val from: User,
    val currency: String,
    @JsonProperty("total_amount") val totalAmount: BigInteger,
    @JsonProperty("invoice_payload") val invoicePayload: String,
    @JsonProperty("shipping_option_id") val shippingOptionId: String?,
    @JsonProperty("order_info") val orderInfo: OrderInfo?
)
