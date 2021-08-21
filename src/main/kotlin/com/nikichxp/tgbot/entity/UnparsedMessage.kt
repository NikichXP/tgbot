package com.nikichxp.tgbot.entity

import org.bson.Document
import java.util.*

@org.springframework.data.mongodb.core.mapping.Document("unparsedMessage")
data class UnparsedMessage(
        var content: Document,
        var created: Long = System.currentTimeMillis()
) {

    var id = UUID.randomUUID().toString()

}