package com.nikichxp.tgbot.dto

import com.fasterxml.jackson.annotation.JsonProperty as Name

data class ResponseParameters(
    @Name("migrate_to_chat_id") val migrateToChatId: Long? = null,
    @Name("retry_after") val replyAfter: Long? = null
)
