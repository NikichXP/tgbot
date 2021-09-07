package com.nikichxp.tgbot.dto.files

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents an audio file to be treated as music by the Telegram clients.
 * https://core.telegram.org/bots/api#audio
 */
data class Audio(
    @JsonProperty(FilesFields.fileId) val fileId: String,
    @JsonProperty(FilesFields.fileUniqueId) val fileUniqueId: String,
    @JsonProperty(FilesFields.duration) val duration: Int,
    @JsonProperty(FilesFields.performer) val performer: String? = null,
    @JsonProperty(FilesFields.title) val title: String? = null,
    @JsonProperty(FilesFields.mimeType) val mimeType: String? = null,
    @JsonProperty(FilesFields.fileSize) val fileSize: Int? = null,
    @JsonProperty(FilesFields.thumb) val thumb: PhotoSize? = null,
    @JsonProperty(FilesFields.fileName) val fileName: String? = null
)
