package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.springframework.stereotype.Component

@Component
class VersionHandler(
    private val tgOperations: TgOperations,
    private val versionProvider: VersionProvider
) : CommandHandler {

    override fun supportedBots() = TgBot.entries.toSet()

//    override fun isCommandSupported(command: String) = command in listOf("/version", "/v", "/buildinfo")

    // TODO add other commands, make it array
    @HandleCommand("/version")
    suspend fun processCommand(args: List<String>, update: Update): Boolean {
        tgOperations.replyToCurrentMessage("version: ${versionProvider.appVersion}")
        return true
    }



}

