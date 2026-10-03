package com.nikichxp.tgbot.core.util

import com.nikichxp.tgbot.core.entity.common.UserModel

object UserFormatter {

    fun getUserPrintName(user: UserModel): String {
        return user.username ?: user.fullName
    }

}