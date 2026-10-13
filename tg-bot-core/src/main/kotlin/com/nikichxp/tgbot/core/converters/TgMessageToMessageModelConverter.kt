package com.nikichxp.tgbot.core.converters

import com.nikichxp.bot.common.entity.model.MessageModel
import com.nikichxp.tgbot.core.dto.Message
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class TgMessageToMessageModelConverter(
    private val voiceConverter: TgVoiceToVoiceModelConverter,
    private val stickerConverter: TgStickerToStickerModelConverter
) : Converter<Message, MessageModel> {

    override fun convert(source: Message) = MessageModel(
        id = source.messageId,
        text = source.text,
        voice = source.voice?.let(voiceConverter::convert),
        sticker = source.sticker?.let(stickerConverter::convert)
    )
}
