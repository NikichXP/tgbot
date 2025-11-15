package com.nikichxp.tgbot.core.service.tgapi

import java.time.LocalDateTime

data class BotLastKnownMessage(var id: String, var updateId: Long, var date: LocalDateTime = LocalDateTime.now())
