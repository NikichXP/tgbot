package com.nikichxp.tgbot.karmabot.entity

import com.nikichxp.bot.common.entity.model.UserModel
import org.springframework.data.annotation.Id

data class UserInfo(
    @Id val id: Long,
    var user: UserModel? = null,
    var rating: Double = 0.0
)
