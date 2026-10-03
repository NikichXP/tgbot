package com.nikichxp.tgbot.core.converters

import com.nikichxp.tgbot.core.dto.stickers.Sticker
import com.nikichxp.tgbot.core.entity.common.StickerModel
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class TgStickerToStickerModelConverter : Converter<Sticker, StickerModel> {

    override fun convert(source: Sticker) = StickerModel(fileId = source.fileId, emoji = source.emoji)
}
