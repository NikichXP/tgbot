package com.nikichxp.tgbot.childcarebot.logic

import com.nikichxp.bot.common.util.IAppStorage
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

// TODO This should become a cross-app service
@Service
class ChildTimezoneService(
    private val appStorage: IAppStorage
) {

    fun fromDBToUI(date: LocalDateTime): LocalDateTime {
        return date
            .atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneId.of(getUserTimeZone()))
            .toLocalDateTime()
    }

    fun fromUItoDB(date: LocalDateTime): LocalDateTime {
        return date
            .atZone(ZoneId.of(getUserTimeZone()))
            .withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    fun nowInUI(): LocalDateTime {
        return ZonedDateTime.now()
            .withZoneSameInstant(ZoneId.of(getUserTimeZone())).toLocalDateTime()
    }

    fun nowInDB(): LocalDateTime {
        return ZonedDateTime.now()
            .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    }

    private fun getUserTimeZone() = appStorage.getOrPut("timezone.child.user", "UTC+1")

}