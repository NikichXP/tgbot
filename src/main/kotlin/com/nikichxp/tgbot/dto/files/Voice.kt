package com.nikichxp.tgbot.dto.files

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a voice note.
 * https://core.telegram.org/bots/api#voice
 */
data class Voice(
    @JsonProperty(FilesFields.fileId) val fileId: String,
    @JsonProperty(FilesFields.fileUniqueId) val fileUniqueId: String,
    @JsonProperty(FilesFields.duration) val duration: Int,
    @JsonProperty(FilesFields.mimeType) val mimeType: String? = null,
    @JsonProperty(FilesFields.fileSize) val fileSize: Int? = null
)
