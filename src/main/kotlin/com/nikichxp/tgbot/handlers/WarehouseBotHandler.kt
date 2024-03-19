package com.nikichxp.tgbot.handlers

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.handlers.commands.CommandHandler
import com.nikichxp.tgbot.service.tgapi.TgOperations
import com.nikichxp.tgbot.warehousebot.WarehouseConnector
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class WarehouseBotHandler(
    private val warehouseConnector: WarehouseConnector,
    private val tgOperations: TgOperations
) : CommandHandler {

    private val commands = mapOf(
        "/list" to ::list,
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

    private suspend fun list(update: Update, args: List<String>) {
        val userId = update.message?.from?.id?.toString() ?: return
        val list = warehouseConnector.listWarehouseEntities(userId)
        tgOperations.sendMessage(
            chatId = update.message.chat.id,
            update = update,
            text = list.joinToString("\n")
        )
    }
}