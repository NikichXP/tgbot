package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Describes the options used for link preview generation.
 * https://core.telegram.org/bots/api#linkpreviewoptions
 */
data class LinkPreviewOptions(
    @JsonProperty("is_disabled") val isDisabled: Boolean? = null,
    val url: String? = null,
    @JsonProperty("prefer_small_media") val preferSmallMedia: Boolean? = null,
    @JsonProperty("prefer_large_media") val preferLargeMedia: Boolean? = null,
    @JsonProperty("show_above_text") val showAboveText: Boolean? = null
)
