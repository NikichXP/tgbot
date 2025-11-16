package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import org.springframework.stereotype.Component

@Component
class VersionHandler(
    private val tgMessageService: TgMessageService,
    private val versionProvider: VersionProvider
) : CommandHandler {

    override fun requiredFeatures() = setOf(Features.DEBUG)

    // TODO add other commands, make it array
    @HandleCommand("/version")
    suspend fun processCommand(): Boolean {
        tgMessageService.replyToCurrentMessage("version: ${versionProvider.appVersion}")
        return true
    }



}

