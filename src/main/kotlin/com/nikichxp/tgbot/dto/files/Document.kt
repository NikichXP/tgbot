package com.nikichxp.tgbot.dto.files

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a general file (as opposed to photos, voice messages and audio files).
 * https://core.telegram.org/bots/api#document
 */
data class Document(
    @JsonProperty(FilesFields.fileId) val fileId: String,
    @JsonProperty(FilesFields.fileUniqueId) val fileUniqueId: String,
    @JsonProperty(FilesFields.thumb) val thumb: PhotoSize? = null,
    @JsonProperty(FilesFields.fileName) val fileName: String? = null,
    @JsonProperty(FilesFields.mimeType) val mimeType: String? = null,
    @JsonProperty(FilesFields.fileSize) val fileSize: Int? = null
)
