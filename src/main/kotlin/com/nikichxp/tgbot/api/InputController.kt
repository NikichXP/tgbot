package com.nikichxp.tgbot.api

import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.service.RawMessageParser
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class InputController(
    private val rawMessageParser: RawMessageParser
) {

    private val botMap = TgBot.values().associateBy { it.botName }

    @PostMapping("/handle/{bot}")
    fun handle(@PathVariable bot: String, @RequestBody body: Document): String {
        val botEntity = botMap[bot] ?: throw IllegalArgumentException()
        rawMessageParser.proceedRawData(body, botEntity)
        return "ok"
    }
}