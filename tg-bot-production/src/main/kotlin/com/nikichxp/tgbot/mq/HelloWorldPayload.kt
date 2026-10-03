package com.nikichxp.tgbot.mq

import java.time.Instant

data class HelloWorldPayload(
    val message: String = "Hello, world!",
    val timestamp: Instant = Instant.now()
)
