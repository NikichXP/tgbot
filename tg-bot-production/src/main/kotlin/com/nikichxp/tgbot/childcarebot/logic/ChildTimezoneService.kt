package com.nikichxp.tgbot.childcarebot.logic

import com.nikichxp.tgbot.core.util.AppStorage
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

// TODO This should become a cross-app service
@Service
class ChildTimezoneService(
    private val appStorage: AppStorage
) {

    fun fromDBToUI(date: LocalDateTime): LocalDateTime {
        return date
            .atZone(ZoneId.of(getHostTimeZone()))
            .withZoneSameInstant(ZoneId.of(getUserTimeZone()))
            .toLocalDateTime()
    }

    fun fromUItoDB(date: LocalDateTime): LocalDateTime {
        return date
            .atZone(ZoneId.of(getUserTimeZone()))
            .withZoneSameInstant(ZoneId.of(getHostTimeZone()))
            .toLocalDateTime()
    }

    fun nowInUI(): LocalDateTime {
        return ZonedDateTime.now()
            .withZoneSameInstant(ZoneId.of(getUserTimeZone())).toLocalDateTime()
    }

    fun nowInDB(): LocalDateTime {
        return ZonedDateTime.now()
            .withZoneSameInstant(ZoneId.of(getHostTimeZone())).toLocalDateTime()
    }

    private fun getHostTimeZone() = appStorage.getOrPut("timezone.child.host", "UTC+2")
    private fun getUserTimeZone() = appStorage.getOrPut("timezone.child.user", "UTC+1")

}