package com.nikichxp.tgbot.entity

import org.bson.Document
import org.springframework.data.mongodb.core.index.Indexed
import java.time.LocalDateTime
import java.util.*

@org.springframework.data.mongodb.core.mapping.Document("unparsedMessage")
data class UnparsedMessage(
        var content: Document,
        var message: String? = null,
        @Indexed(name = "time_limited_index", expireAfter = "7d")
        var created: LocalDateTime = LocalDateTime.now()
) {

    var id = UUID.randomUUID().toString()

}