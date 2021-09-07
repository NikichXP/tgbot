package com.nikichxp.tgbot.dto

import com.nikichxp.tgbot.dto.files.PhotoSize
import com.fasterxml.jackson.annotation.JsonProperty as Name

data class UserProfilePhotos(
    @Name("total_count") val totalCount: Int,
    val photos: List<List<PhotoSize>>
)
