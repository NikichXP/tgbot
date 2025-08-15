package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.entity.bots.TgBot
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.springframework.stereotype.Component

@Component
class VersionHandler(
    private val tgOperations: TgOperations,
    private val versionProvider: VersionProvider
) : CommandHandler {

    override fun requiredFeatures() = setOf(Features.DEBUG)

    override fun supportedBots() = TgBot.entries.toSet()

    // TODO add other commands, make it array
    @HandleCommand("/version")
    suspend fun processCommand(): Boolean {
        tgOperations.replyToCurrentMessage("version: ${versionProvider.appVersion}")
        return true
    }



}

