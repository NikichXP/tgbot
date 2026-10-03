package com.nikichxp.tgbot.people.service

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class WriteThrottle(
    private val minInterval: Duration,
    private val clock: Clock = Clock.systemUTC()
) {

    private val lastWrites = ConcurrentHashMap<String, Instant>()

    fun tryAcquire(key: String): Boolean {
        val now = clock.instant()
        var acquired = false
        lastWrites.compute(key) { _, lastWrite ->
            if (lastWrite == null || Duration.between(lastWrite, now) >= minInterval) {
                acquired = true
                now
            } else {
                lastWrite
            }
        }
        return acquired
    }
}
