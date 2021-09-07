package com.nikichxp.tgbot.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class WebhookInfo(
    @JsonProperty("url") val url: String,
    @JsonProperty("has_custom_certificate") val hasCustomCertificate: Boolean,
    @JsonProperty("pending_update_count") val pendingUpdateCount: Int,
    @JsonProperty("last_error_date") val lastErrorDate: Long?,
    @JsonProperty("last_error_message") val lastErrorMessage: String?,
    @JsonProperty("max_connections") val maxConnections: Int?,
    @JsonProperty("allowed_updates") val allowedUpdates: List<String>?
)
