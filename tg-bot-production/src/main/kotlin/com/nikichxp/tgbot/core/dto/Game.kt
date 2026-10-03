package com.nikichxp.tgbot.core.dto

import com.nikichxp.tgbot.core.dto.files.Animation
import com.nikichxp.tgbot.core.dto.files.PhotoSize
import com.fasterxml.jackson.annotation.JsonProperty as Name

data class Game(
    val title: String,
    val description: String,
    val photo: List<PhotoSize>,
    val text: String? = null,
    @Name("text_entities") val textEntities: List<MessageEntity>? = null,
    val animation: Animation? = null
)
