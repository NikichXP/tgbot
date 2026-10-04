package com.nikichxp.tgbot.debug.handler

import com.nikichxp.tgbot.core.auth.ITrustedUserService
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.ITgMessageService
import com.nikichxp.tgbot.debug.service.MemoryTrackerService
import org.springframework.stereotype.Component

@Component
class MemStatusHandler(
    private val memoryTrackerService: MemoryTrackerService,
    private val tgMessageService: ITgMessageService,
    private val trustedUserService: ITrustedUserService
) : CommandHandler, Authenticable {

    override fun requiredFeatures() = setOf(Features.DEBUG)

    override suspend fun authenticate(context: UpdateContext): Boolean = trustedUserService.isAdmin(context)

    @HandleCommand("/memstatus")
    suspend fun printMemoryStatus(): Boolean {
        tgMessageService.replyToCurrentMessage(memoryTrackerService.getMemoryStatus().prettyPrint())
        return true
    }
}

