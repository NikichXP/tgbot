package com.nikichxp.bot.common.service

interface IAdminNotificationService {
    suspend fun notifyAdmin(message: String)
}
