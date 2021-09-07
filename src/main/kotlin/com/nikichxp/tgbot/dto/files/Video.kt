package com.nikichxp.tgbot.dto.files

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a video file.
 * https://core.telegram.org/bots/api#video
 */
data class Video(
    @JsonProperty(FilesFields.fileId) val fileId: String,
    @JsonProperty(FilesFields.fileUniqueId) val fileUniqueId: String,
    @JsonProperty(FilesFields.width) val width: Int,
    @JsonProperty(FilesFields.height) val height: Int,
    @JsonProperty(FilesFields.duration) val duration: Int,
    @JsonProperty(FilesFields.thumb) val thumb: PhotoSize? = null,
    @JsonProperty(FilesFields.mimeType) val mimeType: String? = null,
    @JsonProperty(FilesFields.fileSize) val fileSize: Int? = null,
    @JsonProperty(FilesFields.fileName) val fileName: String? = null
)
