package com.nikichxp.tgbot.warehousebot

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.springframework.stereotype.Service

@Service
class WarehouseBotCommandHandler(
    private val warehouseService: WarehouseService,
    private val tgOperations: TgOperations
) : CommandHandler, UpdateHandler {

    private val commands = mapOf<String, suspend (Update, List<String>) -> Unit>(
        "/list" to { update, _ -> renderText(update) { warehouseService.list(update) } },
        "/get" to { update, args -> renderText(update) { warehouseService.get(update, args.first()) } },
        "/create" to { update, args -> },
        "/update" to { update, args -> }
    )

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.MESSAGE)

    override fun requiredFeatures() = setOf(Features.WAREHOUSE)

    override fun canHandle(update: Update): Boolean {
        return update.message?.text?.startsWith("/") ?: false
    }

    override suspend fun handleUpdate(update: Update) {
        // TODO implement this
    }

//    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
//        runBlocking {
//            commands[command]?.invoke(update, args)
//        }
//        return true
//    }
//
//    override fun isCommandSupported(command: String): Boolean = setOf(
//        "/list",
//        "/get",
//        "/create",
//        "/update"
//    ).contains(command)

    private suspend fun renderText(update: Update, supplier: suspend () -> List<String>) {
        tgOperations.replyToCurrentMessage(supplier().joinToString("\n"))
    }

}