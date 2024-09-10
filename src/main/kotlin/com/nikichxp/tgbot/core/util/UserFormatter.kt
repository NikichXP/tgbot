package com.nikichxp.tgbot.core.util

import com.nikichxp.tgbot.core.dto.User

object UserFormatter {

    fun getUserPrintName(user: User): String {
        return when {
            user.username != null -> user.username
            user.lastName != null -> "${user.firstName} ${user.lastName}"
            else -> user.firstName
        }
    }

}