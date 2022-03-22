package com.nikichxp.tgbot.dto.stickers

import com.nikichxp.tgbot.dto.files.FilesFields
import com.nikichxp.tgbot.dto.files.PhotoSize
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a sticker.
 * https://core.telegram.org/bots/api#sticker
 */
data class Sticker(
    @JsonProperty(FilesFields.fileId) val fileId: String,
    @JsonProperty(FilesFields.fileUniqueId) val fileUniqueId: String,
    @JsonProperty(FilesFields.width) val width: Int,
    @JsonProperty(FilesFields.height) val height: Int,
    @JsonProperty(FilesFields.isAnimated) val isAnimated: Boolean,
    @JsonProperty(FilesFields.isVideo) val isVideo: Boolean,
    @JsonProperty(FilesFields.thumb) val thumb: PhotoSize? = null,
    @JsonProperty(FilesFields.emoji) val emoji: String?,
    @JsonProperty(FilesFields.setName)val setName: String? = null,
    @JsonProperty(FilesFields.maskPosition) val maskPosition: MaskPosition? = null,
    @JsonProperty(FilesFields.fileSize) val fileSize: Int? = null
)
