package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Contains basic information about a refunded payment.
 * https://core.telegram.org/bots/api#refundedpayment
 */
data class RefundedPayment(
    val currency: String,
    @JsonProperty("total_amount") val totalAmount: Int,
    @JsonProperty("invoice_payload") val invoicePayload: String,
    @JsonProperty("telegram_payment_charge_id") val telegramPaymentChargeId: String,
    @JsonProperty("provider_payment_charge_id") val providerPaymentChargeId: String? = null
)
