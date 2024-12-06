package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.springframework.stereotype.Service

@Service
class ChildCareCommandHandler(
    private val tgOperations: TgOperations
) : CommandHandler {
    override fun supportedBots(): Set<TgBot> = setOf(TgBot.NIKICHBOT)

    @HandleCommand("/status")
    suspend fun status() {
        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "I'm alive!"
        }
    }

}