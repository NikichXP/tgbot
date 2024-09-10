package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty as Name

data class ForceReplyMarkup(
    @Name("force_reply") val forceReply: Boolean = true,
    val selective: Boolean? = null
) : ReplyMarkup