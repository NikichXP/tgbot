package com.nikichxp.tgbot.core.service

interface IAdminNotificationService {
    suspend fun notifyAdmin(message: String)
}
