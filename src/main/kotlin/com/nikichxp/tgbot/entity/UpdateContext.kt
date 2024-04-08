package com.nikichxp.tgbot.entity

import com.nikichxp.tgbot.dto.Update

data class UpdateContext(
    val update: Update,
    val tgBot: TgBot
)
