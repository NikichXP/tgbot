package com.nikichxp.tgbot.core.service.tgapi

import com.fasterxml.jackson.annotation.JsonProperty

data class TgSetWebhookParams(val url: String)

data class TgDeleteWebhookParams(
    @JsonProperty("drop_pending_updates") val dropPendingUpdates: Boolean = false
)
