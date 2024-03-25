package com.nikichxp.tgbot.handlers.commands

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.service.WarehouseService
import com.nikichxp.tgbot.service.tgapi.TgOperations
import com.nikichxp.tgbot.warehousebot.WarehouseConnector
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class WarehouseBotHandler(
    private val warehouseService: WarehouseService,
    private val warehouseConnector: WarehouseConnector,
    private val tgOperations: TgOperations
) : CommandHandler {

    private val commands = mapOf<String, suspend (Update, List<String>) -> Unit> (
        "/list" to { update, args -> renderText(update) { warehouseService.list(update, args) } },
        "/get" to { update, args -> },
        "/create" to { update, args -> },
        "/update" to { update, args -> }
    )

    override fun supportedBots(tgBot: TgBot): Set<TgBot> = setOf(TgBot.ALLMYSTUFFBOT)

    override fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        runBlocking {
            commands[command]?.invoke(update, args)
        }
        return true
    }

    override fun isCommandSupported(command: String): Boolean = commands.containsKey(command)

    private suspend fun renderText(update: Update, supplier: suspend () -> List<String>) {
        tgOperations.replyToCurrentMessage(supplier().joinToString("\n"), update)
    }

}