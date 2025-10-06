package com.nikichxp.tgbot.childcarebot

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class ChildReplyHandlerTest {

    @Test
    fun `patterns matching`() {
        val regex = ChildReplyHandler.TIME_PATTERN.toRegex()
        assertThat(regex.matches("12:34")).isTrue
        assertThat(regex.matches("12:34:56")).isFalse
        assertThat(regex.matches("12:34:56:56")).isFalse
        assertThat(regex.matches("1:15")).isTrue
        assertThat(regex.matches("1:15:15")).isFalse
    }

}