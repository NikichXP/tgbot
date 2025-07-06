package com.nikichxp.tgbot.core.entity.bots

import org.springframework.data.annotation.Id

data class BotConfig(
    @Id val botName: String,
    val token: String
)
