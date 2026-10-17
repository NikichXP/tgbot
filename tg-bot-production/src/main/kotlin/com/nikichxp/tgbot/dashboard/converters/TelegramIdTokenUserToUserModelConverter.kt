package com.nikichxp.tgbot.dashboard.converters

import com.nikichxp.bot.common.entity.model.UserModel
import com.nikichxp.tgbot.dashboard.dto.TelegramIdTokenUser
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class TelegramIdTokenUserToUserModelConverter : Converter<TelegramIdTokenUser, UserModel> {

    override fun convert(source: TelegramIdTokenUser) = UserModel(
        id = source.id,
        username = source.username,
        fullName = source.name ?: ""
    )
}
