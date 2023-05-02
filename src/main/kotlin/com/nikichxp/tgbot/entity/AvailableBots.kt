package com.nikichxp.tgbot.entity

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

enum class TgBot {
    NIKICHBOT, ALLMYSTUFFBOT
}

@Configuration
class TgBotConfig {

    @Value("\${app.tokens.nikichbot}")
    lateinit var nikichBotToken: String
    @Value("\${app.tokens.allmystuffbot}")
    lateinit var allMyStuffBotToken: String

}