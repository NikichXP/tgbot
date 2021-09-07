package com.nikichxp.tgbot.controller

import com.nikichxp.tgbot.service.MessageParser
import org.bson.Document
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class InputController(
    private val messageParser: MessageParser
) {

    @PostMapping("/handle")
    fun handle(@RequestBody body: Document): String {
        messageParser.proceedRawData(body)
        return "ok"
    }
}