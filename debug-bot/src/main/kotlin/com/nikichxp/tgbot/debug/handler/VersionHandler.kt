package com.nikichxp.tgbot.debug.handler

import com.nikichxp.tgbot.core.auth.ITrustedUserService
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.ITgMessageService
import com.nikichxp.tgbot.debug.service.VersionProvider
import org.springframework.stereotype.Component

@Component
class VersionHandler(
    private val tgMessageService: ITgMessageService,
    private val versionProvider: VersionProvider,
    private val trustedUserService: ITrustedUserService
) : CommandHandler, Authenticable {

    override fun requiredFeatures() = setOf(Features.DEBUG)

    override suspend fun authenticate(context: UpdateContext): Boolean = trustedUserService.isAdmin(context)

    // TODO add other commands, make it array
    @HandleCommand("/version")
    suspend fun processCommand(): Boolean {
        tgMessageService.replyToCurrentMessage("version: ${versionProvider.appVersion}")
        return true
    }

}

