package com.nikichxp.tgbot.core.auth

import com.nikichxp.tgbot.core.entity.UpdateContext

interface ITrustedUserService {
    fun isTrusted(userId: Long?, username: String?): Boolean
    fun isTrusted(context: UpdateContext): Boolean
    fun isAdmin(userId: Long?): Boolean
    fun isAdmin(context: UpdateContext): Boolean
}
