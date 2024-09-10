package com.nikichxp.tgbot.core.dto

import com.nikichxp.tgbot.core.dto.files.PhotoSize
import com.fasterxml.jackson.annotation.JsonProperty as Name

data class UserProfilePhotos(
    @Name("total_count") val totalCount: Int,
    val photos: List<List<PhotoSize>>
)
