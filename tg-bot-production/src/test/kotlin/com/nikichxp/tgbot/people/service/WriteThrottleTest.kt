package com.nikichxp.tgbot.people.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class WriteThrottleTest {

    private class MutableClock(var now: Instant) : Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?) = this
        override fun instant() = now
    }

    private val clock = MutableClock(Instant.parse("2026-09-30T12:00:00Z"))
    private val throttle = WriteThrottle(Duration.ofMinutes(10), clock)

    @Test
    fun `first write for a key is allowed`() {
        assertThat(throttle.tryAcquire("a")).isTrue()
    }

    @Test
    fun `repeated write within interval is suppressed`() {
        throttle.tryAcquire("a")
        clock.now = clock.now.plus(Duration.ofMinutes(9))
        assertThat(throttle.tryAcquire("a")).isFalse()
    }

    @Test
    fun `write is allowed again after interval`() {
        throttle.tryAcquire("a")
        clock.now = clock.now.plus(Duration.ofMinutes(10))
        assertThat(throttle.tryAcquire("a")).isTrue()
    }

    @Test
    fun `keys are throttled independently`() {
        throttle.tryAcquire("a")
        assertThat(throttle.tryAcquire("b")).isTrue()
    }

    @Test
    fun `suppressed attempt does not extend the interval`() {
        throttle.tryAcquire("a")
        clock.now = clock.now.plus(Duration.ofMinutes(5))
        throttle.tryAcquire("a")
        clock.now = clock.now.plus(Duration.ofMinutes(5))
        assertThat(throttle.tryAcquire("a")).isTrue()
    }
}
