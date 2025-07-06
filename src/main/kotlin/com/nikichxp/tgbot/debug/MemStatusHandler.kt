package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.entity.bots.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.MemoryTrackerService
import org.springframework.stereotype.Component

@Component
class MemStatusHandler(
    private val memoryTrackerService: MemoryTrackerService,
    private val tgOperations: TgOperations,
) : CommandHandler {

    override fun supportedBots(): Set<TgBot> = setOf(TgBot.NIKICHBOT)

    @HandleCommand("/memstatus")
    suspend fun printMemoryStatus(): Boolean {
        tgOperations.replyToCurrentMessage(memoryTrackerService.getMemoryStatus().prettyPrint())
        return true
    }
}

