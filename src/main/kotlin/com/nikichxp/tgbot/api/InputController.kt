package com.nikichxp.tgbot.api

import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.service.MessageEntryPoint
import org.bson.Document
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class InputController(
    private val messageEntryPoint: MessageEntryPoint
) {

    private val botMap = TgBot.values().associateBy { it.botName }

    @PostMapping("/handle/{bot}")
    fun handle(@PathVariable bot: String, @RequestBody body: Document): String {
        val botEntity = botMap[bot] ?: throw IllegalArgumentException()
        messageEntryPoint.proceedRawData(body, botEntity)
        return "ok"
    }
}