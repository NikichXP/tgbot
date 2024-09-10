package com.nikichxp.tgbot.core.dto.payments

import java.math.BigInteger

data class LabeledPrice(
    val label: String? = null,
    val amount: BigInteger
)
