package com.nikichxp.tgbot.handlers.commands

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.entity.UpdateContext
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.handlers.UpdateHandler
import com.nikichxp.tgbot.service.WarehouseService
import com.nikichxp.tgbot.service.tgapi.TgOperations
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class WarehouseBotHandler(
    private val warehouseService: WarehouseService,
    private val tgOperations: TgOperations
) : CommandHandler, UpdateHandler {

    private val commands = mapOf<String, suspend (Update, List<String>) -> Unit>(
        "/list" to { update, _ -> renderText(update) { warehouseService.list(update) } },
        "/get" to { update, args -> renderText(update) { warehouseService.get(update, args.first()) } },
        "/create" to { update, args -> },
        "/update" to { update, args -> }
    )

    override fun botSupported(bot: TgBot): Boolean = true

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.MESSAGE)

    override fun supportedBots(tgBot: TgBot): Set<TgBot> = setOf(TgBot.ALLMYSTUFFBOT)

    override fun canHandle(context: UpdateContext): Boolean {
        return context.update.message?.text?.startsWith("/") ?: false
    }

    override suspend fun handleUpdate(update: Update) {
        // TODO implement this
    }

    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        runBlocking {
            commands[command]?.invoke(update, args)
        }
        return true
    }

    override fun isCommandSupported(command: String): Boolean = true

    private suspend fun renderText(update: Update, supplier: suspend () -> List<String>) {
        tgOperations.replyToCurrentMessage(supplier().joinToString("\n"))
    }

}