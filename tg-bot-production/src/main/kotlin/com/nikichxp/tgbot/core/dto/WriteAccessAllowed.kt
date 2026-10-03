package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a service message about a user allowing a bot to write messages after adding it to the attachment menu.
 * https://core.telegram.org/bots/api#writeaccessallowed
 */
data class WriteAccessAllowed(
    @JsonProperty("from_request") val fromRequest: Boolean? = null,
    @JsonProperty("web_app_name") val webAppName: String? = null,
    @JsonProperty("from_attachment_menu") val fromAttachmentMenu: Boolean? = null
)
