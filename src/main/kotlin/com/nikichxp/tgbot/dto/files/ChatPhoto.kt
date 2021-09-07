package com.nikichxp.tgbot.dto.files

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a chat photo.
 * https://core.telegram.org/bots/api#chatphoto
 */
data class ChatPhoto(
    @JsonProperty(FilesFields.smallFileId) val smallFileId: String,
    @JsonProperty(FilesFields.smallFileUniqueId) val smallFileUniqueId: String,
    @JsonProperty(FilesFields.bigFileId) val bigFileId: String,
    @JsonProperty(FilesFields.bigFileUniqueId) val bigFileUniqueId: String
)
