package com.nikichxp.tgbot.entity

import org.bson.Document
import java.util.*

data class UnparsedMessage(
        var content: Document,
        var created: Long = System.currentTimeMillis()
) {

    var id = UUID.randomUUID().toString()

}