package com.nikichxp.tgbot.core.handlers.commands

import com.nikichxp.tgbot.core.entity.TgBot
import org.springframework.stereotype.Indexed

@Indexed
interface CommandHandler {
    fun supportedBots(): Set<TgBot>
}