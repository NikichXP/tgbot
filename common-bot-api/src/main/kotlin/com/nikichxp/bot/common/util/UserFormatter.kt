package com.nikichxp.bot.common.util

import com.nikichxp.bot.common.entity.model.UserModel

object UserFormatter {

    fun displayName(user: UserModel): String = user.username ?: user.fullName

    fun mention(user: UserModel): String = user.username?.let { "@$it" } ?: user.fullName

    fun diagnostic(user: UserModel, prefix: String): String = listOfNotNull(
        "$prefix.id:${user.id}",
        user.username?.let { "$prefix.username:$it" },
        "$prefix.fullName:${user.fullName}"
    ).joinToString(" | ")

}
