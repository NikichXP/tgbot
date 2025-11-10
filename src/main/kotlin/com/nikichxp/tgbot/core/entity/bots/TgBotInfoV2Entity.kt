package com.nikichxp.tgbot.core.entity.bots

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "tgBotInfo")
data class TgBotInfoV2Entity(@Id var name: String) {

    lateinit var token: String

    var supportedFeatures = setOf<String>()
}