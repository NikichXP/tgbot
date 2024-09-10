package com.nikichxp.tgbot.core.dto.stickers

import com.nikichxp.tgbot.core.dto.files.PhotoSize
import com.fasterxml.jackson.annotation.JsonProperty as Name

data class StickerSet(
    val name: String,
    val title: String,
    @Name("is_animated") val isAnimated: Boolean,
    @Name("contains_masks") val containsMasks: Boolean,
    @Name("stickers") val stickers: List<Sticker>,
    val thumb: PhotoSize?
)
