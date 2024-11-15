package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandlerV2
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.MemoryTrackerService
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class MemStatusHandler(
    private val memoryTrackerService: MemoryTrackerService,
    private val tgOperations: TgOperations,
) : CommandHandlerV2() {

    override fun supportedBots(tgBot: TgBot): Set<TgBot> = setOf(TgBot.NIKICHBOT)

    @PostConstruct
    fun setupHandlers() {
        registerCommand("/memstatus") { _, _ ->
            tgOperations.replyToCurrentMessage(memoryTrackerService.getMemoryStatus().prettyPrint())
            true
        }
    }
}