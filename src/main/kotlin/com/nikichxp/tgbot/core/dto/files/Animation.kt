package com.nikichxp.tgbot.core.dto.files

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents an animation file (GIF or H.264/MPEG-4 AVC video without sound).
 * https://core.telegram.org/bots/api#animation
 */
data class Animation(
    @JsonProperty(FilesFields.fileId) val fileId: String,
    @JsonProperty(FilesFields.fileUniqueId) val fileUniqueId: String,
    @JsonProperty(FilesFields.width) val width: Int,
    @JsonProperty(FilesFields.height) val height: Int,
    @JsonProperty(FilesFields.duration) val duration: Int,
    @JsonProperty(FilesFields.thumb) val thumb: PhotoSize? = null,
    @JsonProperty(FilesFields.fileName) val fileName: String? = null,
    @JsonProperty(FilesFields.mimeType) val mimeType: String? = null,
    @JsonProperty(FilesFields.fileSize) val fileSize: Long? = null
)
