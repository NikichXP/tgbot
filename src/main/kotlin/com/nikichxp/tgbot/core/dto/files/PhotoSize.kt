package com.nikichxp.tgbot.core.dto.files

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents one size of a photo or a file / sticker thumbnail.
 * https://core.telegram.org/bots/api#photosize
 */
data class PhotoSize(
    @JsonProperty(FilesFields.fileId) val fileId: String,
    @JsonProperty(FilesFields.fileUniqueId) val fileUniqueId: String,
    @JsonProperty(FilesFields.width) val width: Int,
    @JsonProperty(FilesFields.height) val height: Int,
    @JsonProperty(FilesFields.fileSize) val fileSize: Int? = null
)
