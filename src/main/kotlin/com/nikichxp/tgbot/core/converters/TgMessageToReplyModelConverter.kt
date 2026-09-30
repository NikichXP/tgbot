package com.nikichxp.tgbot.core.converters

import com.nikichxp.tgbot.core.dto.Message
import com.nikichxp.tgbot.core.entity.common.ReplyModel
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class TgMessageToReplyModelConverter(
    private val userConverter: TgUserToUserModelConverter,
    private val chatConverter: TgChatToChatModelConverter
) : Converter<Message, ReplyModel> {

    override fun convert(source: Message) = ReplyModel(
        messageId = source.messageId,
        text = source.text,
        from = source.from?.let(userConverter::convert),
        chat = chatConverter.convert(source.chat)
    )
}
