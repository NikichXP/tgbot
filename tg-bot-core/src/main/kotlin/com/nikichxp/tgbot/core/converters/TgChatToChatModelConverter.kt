package com.nikichxp.tgbot.core.converters

import com.nikichxp.bot.common.entity.model.ChatModel
import com.nikichxp.tgbot.core.dto.Chat
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class TgChatToChatModelConverter : Converter<Chat, ChatModel> {

    override fun convert(source: Chat) = ChatModel(
        id = source.id,
        type = source.type,
        title = source.title ?: listOfNotNull(source.firstName, source.lastName).joinToString(" ")
    )
}
