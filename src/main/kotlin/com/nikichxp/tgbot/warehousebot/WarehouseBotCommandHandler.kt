package com.nikichxp.tgbot.warehousebot

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import org.springframework.stereotype.Service

@Service
class WarehouseBotCommandHandler(
    private val warehouseService: WarehouseService,
    private val tgMessageService: TgMessageService
) : CommandHandler, UpdateHandler {

    private val commands = mapOf<String, suspend (Update, List<String>) -> Unit>(
        "/list" to { update, _ -> renderText(update) { warehouseService.list(update) } },
        "/get" to { update, args -> renderText(update) { warehouseService.get(update, args.first()) } },
        "/create" to { update, args -> },
        "/update" to { update, args -> }
    )

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.MESSAGE)

    override fun requiredFeatures() = setOf(Features.WAREHOUSE)

    override fun canHandle(context: UpdateContext): Boolean {
        return context.message?.text?.startsWith("/") ?: false
    }

    override suspend fun handleUpdate(updateContext: UpdateContext) {
        tgMessageService.sendMessage {
            replyToCurrentMessage()
            text = "This feature is not implemented yet."
        }
    }

    private suspend fun renderText(update: Update, supplier: suspend () -> List<String>) {
        tgMessageService.replyToCurrentMessage(supplier().joinToString("\n"))
    }

}