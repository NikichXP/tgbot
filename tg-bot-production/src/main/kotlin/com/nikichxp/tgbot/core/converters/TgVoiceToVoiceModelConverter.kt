package com.nikichxp.tgbot.core.converters

import com.nikichxp.tgbot.core.dto.files.Voice
import com.nikichxp.tgbot.core.entity.common.VoiceModel
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
