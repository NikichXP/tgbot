package com.nikichxp.tgbot.core.tooling

import org.bson.Document
import java.time.Instant

data class EventTrace(val data: Document, val time: Instant = Instant.now())
