package com.nikichxp.tgbot.dashboard.converters

import com.nikichxp.tgbot.dashboard.dto.KnownUserChatDto
import com.nikichxp.tgbot.dashboard.dto.KnownUserDto
import com.nikichxp.tgbot.people.entity.KnownUser
import com.nikichxp.tgbot.people.entity.KnownUserChat
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class KnownUserToKnownUserDtoConverter : Converter<KnownUser, KnownUserDto> {

    override fun convert(source: KnownUser) = KnownUserDto(
        id = source.id,
        username = source.username,
        fullName = source.fullName,
        languageCode = source.languageCode,
        isPremium = source.isPremium,
        firstSeenAt = source.firstSeenAt.toString(),
        lastSeenAt = source.lastSeenAt.toString(),
        bots = source.bots.sorted(),
        chats = source.chats.values
            .sortedByDescending { it.lastSeenAt }
            .map { it.toDto() }
    )

    private fun KnownUserChat.toDto() = KnownUserChatDto(
        chatId = chatId,
        title = title,
        type = type,
        lastSeenAt = lastSeenAt.toString()
    )
}
