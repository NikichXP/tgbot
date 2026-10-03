package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.nikichxp.tgbot.core.dto.files.PhotoSize
import com.nikichxp.tgbot.core.dto.files.Video

/**
 * Flattened union of PaidMediaPreview/Photo/Video.
 * https://core.telegram.org/bots/api#paidmedia
 */
data class PaidMedia(
    val type: String,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Int? = null,
    val photo: List<PhotoSize>? = null,
    val video: Video? = null
)

/**
 * Describes the paid media added to a message.
 * https://core.telegram.org/bots/api#paidmediainfo
 */
data class PaidMediaInfo(
    @JsonProperty("star_count") val starCount: Int,
    @JsonProperty("paid_media") val paidMedia: List<PaidMedia>
)
