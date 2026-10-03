package com.nikichxp.tgbot.childcarebot

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class UtilsTest {

    @Test
    fun `getDurationStringBetween formats test correctly`() {

        val start = LocalDateTime.of(2025, 1, 1, 10, 0)
        val end = LocalDateTime.of(2025, 1, 1, 12, 30)

        val testing = getDurationStringBetween(start, end)

        Assertions.assertThat(testing).isEqualTo("2h 30m")
    }

    @Test
    fun `getDurationStringBetween ignores seconds`() {

        val start = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
        val end = LocalDateTime.of(2025, 1, 1, 12, 30, 1)

        val testing = getDurationStringBetween(start, end)

        Assertions.assertThat(testing).isEqualTo("2h 30m")
    }

    @Test
    fun `getDurationStringBetween with next day shows more than 24h`() {

        val start = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
        val end = LocalDateTime.of(2025, 1, 2, 12, 30, 1)

        val testing = getDurationStringBetween(start, end)

        Assertions.assertThat(testing).isEqualTo("26h 30m")
    }
}