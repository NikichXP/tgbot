package com.nikichxp.tgbot.core.dto.files

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a file ready to be downloaded. The file can be downloaded via the link
 * https://api.telegram.org/file/bot<token>/<file_path>. It is guaranteed that the
 * link will be valid for at least 1 hour. When the link expires, a new one can be
 * requested by calling getFile.
 * https://core.telegram.org/bots/api#file
 */
data class File(
    @JsonProperty(FilesFields.fileId) val fileId: String,
    @JsonProperty(FilesFields.fileUniqueId) val fileUniqueId: String,
    @JsonProperty(FilesFields.fileSize) val fileSize: Int? = null,
    @JsonProperty(FilesFields.filePath) val filePath: String? = null
)
