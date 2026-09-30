package com.nikichxp.tgbot.core.converters

import com.nikichxp.tgbot.core.dto.User
import com.nikichxp.tgbot.core.entity.common.UserModel
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class TgUserToUserModelConverter : Converter<User, UserModel> {

    override fun convert(source: User) = UserModel(
        id = source.id,
        username = source.username,
        fullName = listOfNotNull(source.firstName, source.lastName).joinToString(" "),
        firstName = source.firstName,
        lastName = source.lastName,
        languageCode = source.languageCode,
        isBot = source.isBot,
        isPremium = source.isPremium
    )
}
