package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.ITgMessageService
import com.nikichxp.tgbot.core.util.MemoryTrackerService
import org.springframework.stereotype.Component

@Component
class MemStatusHandler(
    private val memoryTrackerService: MemoryTrackerService,
    private val tgMessageService: ITgMessageService,
) : CommandHandler {

    override fun requiredFeatures() = setOf(Features.DEBUG)

    @HandleCommand("/memstatus")
    suspend fun printMemoryStatus(): Boolean {
        tgMessageService.replyToCurrentMessage(memoryTrackerService.getMemoryStatus().prettyPrint())
        return true
    }
}

