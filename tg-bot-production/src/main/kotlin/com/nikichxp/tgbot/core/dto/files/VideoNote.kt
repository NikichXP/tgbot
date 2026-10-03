package com.nikichxp.tgbot.core.dto.files

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a video message (available in Telegram apps as of v.4.0).
 * https://core.telegram.org/bots/api#videonote
 */
data class VideoNote(
    @JsonProperty(FilesFields.fileId) val fileId: String,
    @JsonProperty(FilesFields.fileUniqueId) val fileUniqueId: String,
    @JsonProperty(FilesFields.length) val length: Int,
    @JsonProperty(FilesFields.duration) val duration: Int,
    @JsonProperty(FilesFields.thumb) val thumb: PhotoSize? = null,
    @JsonProperty(FilesFields.fileSize) val fileSize: Int? = null
)
