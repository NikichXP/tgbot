package com.nikichxp.tgbot.api

import com.nikichxp.tgbot.service.RawMessageParser
import org.bson.Document
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class InputController(
    private val rawMessageParser: RawMessageParser
) {

    @PostMapping("/handle")
    fun handle(@RequestBody body: Document): String {
        rawMessageParser.proceedRawData(body)
        return "ok"
    }
}