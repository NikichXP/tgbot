package com.nikichxp.bot.common.util

import com.nikichxp.bot.common.entity.model.UserModel

object UserFormatter {

    fun getUserPrintName(user: UserModel): String {
        return user.username ?: user.fullName
    }

}