package com.nikichxp.tgbot.core.service.tgapi

import java.time.LocalDateTime
import java.time.Month

data class BotLastKnownMessage(var id: String, var updateId: Long, var date: LocalDateTime = MIN_DATE) {
    companion object {
        private val MIN_DATE = LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0)
    }
}
