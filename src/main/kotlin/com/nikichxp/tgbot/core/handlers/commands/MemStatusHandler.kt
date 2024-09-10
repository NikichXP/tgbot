package com.nikichxp.tgbot.core.handlers.commands

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.MemoryTrackerService
import org.springframework.stereotype.Component

@Component
class MemStatusHandler(
    private val memoryTrackerService: MemoryTrackerService,
    private val tgOperations: TgOperations
) : CommandHandler {

    override fun supportedBots(tgBot: TgBot): Set<TgBot> = setOf(TgBot.NIKICHBOT)

    override fun isCommandSupported(command: String): Boolean = command.equals("/memstatus", true)

    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        tgOperations.replyToCurrentMessage(memoryTrackerService.getMemoryStatus().prettyPrint())
        return true
    }

}