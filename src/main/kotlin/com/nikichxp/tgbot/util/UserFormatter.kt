package com.nikichxp.tgbot.util

import com.nikichxp.tgbot.dto.User

object UserFormatter {

    fun getUserPrintName(user: User): String {
        return when {
            user.username != null -> user.username
            user.lastName != null -> "${user.firstName} ${user.lastName}"
            else -> user.firstName
        }
    }

}