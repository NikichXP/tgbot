package com.nikichxp.tgbot.core.converters

import com.nikichxp.bot.common.entity.model.VoiceModel
import com.nikichxp.tgbot.core.dto.files.Voice
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class TgVoiceToVoiceModelConverter : Converter<Voice, VoiceModel> {

    override fun convert(source: Voice) = VoiceModel(
        fileId = source.fileId,
        fileUniqueId = source.fileUniqueId,
        duration = source.duration,
        mimeType = source.mimeType
    )
}
