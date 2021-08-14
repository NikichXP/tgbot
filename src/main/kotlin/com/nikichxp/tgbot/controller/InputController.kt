package com.nikichxp.tgbot.controller

import com.nikichxp.tgbot.entity.UnparsedMessage
import org.bson.Document
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class InputController(
    private val mongoTemplate: MongoTemplate
) {

    @PostMapping("/handle")
    fun handle(@RequestBody body: Document): String {
        mongoTemplate.save(UnparsedMessage(body))
        return body.toJson()
    }
}