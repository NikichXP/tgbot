package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Contains information about Telegram Passport data shared with the bot by the user.
 * https://core.telegram.org/bots/api#encryptedpassportelement
 */
data class EncryptedPassportElement(
    val type: String,
    val data: String? = null,
    @JsonProperty("phone_number") val phoneNumber: String? = null,
    val email: String? = null,
    val files: List<TelegramFile>? = null,
    @JsonProperty("front_side") val frontSide: TelegramFile? = null,
    @JsonProperty("reverse_side") val reverseSide: TelegramFile? = null,
    val selfie: TelegramFile? = null,
    val translation: List<TelegramFile>? = null,
    val hash: String
)

/**
 * Contains data required for decrypting and authenticating EncryptedPassportElement.
 * https://core.telegram.org/bots/api#encryptedcredentials
 */
data class EncryptedCredentials(
    val data: String,
    val hash: String,
    val secret: String
)

/**
 * Describes Telegram Passport data shared with the bot by the user.
 * https://core.telegram.org/bots/api#passportdata
 */
data class PassportData(
    val data: List<EncryptedPassportElement>,
    val credentials: EncryptedCredentials
)
