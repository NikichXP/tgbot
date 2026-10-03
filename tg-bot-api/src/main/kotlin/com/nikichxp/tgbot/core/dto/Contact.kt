package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty as Name

data class Contact(
    @Name("phone_number") val phoneNumber: String,
    @Name("first_name") val firstName: String,
    @Name("last_name") val lastName: String? = null,
    @Name("user_id") val userId: Long? = null
)
