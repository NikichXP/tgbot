package com.nikichxp.tgbot.core.handlers

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.entity.bots.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandlerExecutor
import com.nikichxp.tgbot.core.handlers.commands.CommandHandlerScanner
import com.nikichxp.tgbot.core.handlers.commands.SingleCommandHandler
import com.nikichxp.tgbot.core.util.getContextChatId
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ChatCommandsHandler(
    private val commandHandlerScanner: CommandHandlerScanner,
    private val commandHandlerExecutor: CommandHandlerExecutor,
) : UpdateHandler {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val commandHandlerExecutorMap by lazy {
        commandHandlerScanner.getHandlers().groupBy { it.command }
    }

    override fun requiredFeatures() = setOf<String>()

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.HAS_TEXT)

    override suspend fun handleUpdate(update: Update) {
        val query = update.message!!.text!!.split(" ")
        val command = query.first()
        val args = query.drop(1).filter(String::isNotEmpty)

        coroutineScope {
            val updateContext = this.coroutineContext[UpdateContext] ?: throw IllegalStateException("No update context found")
            val result = commandHandlerExecutorMap[command]?.let {
                it.filter { handler -> isRequiredFeatureSupported(handler, updateContext) }
                    .filter { handler -> if (handler.handler is Authenticable) handler.handler.authenticate(update) else true }
                    .map { handler -> commandHandlerExecutor.execute(handler, args, update) }
            }

            val log = when {
                result == null -> "unknown command"
                result.isEmpty() -> "no handlers for command '$command' handlers for bot ${update.bot}"
                result.all { it } -> "successfully handled command"
                result.any { it } -> "partially handled command (${result.count { it }}/${result.size})"
                else -> "failed executing command"
            }

            logger.info("chadId = ${update.getContextChatId()} | ${update.message.text} | $log")
        }

    }

    private fun isRequiredFeatureSupported(handler: SingleCommandHandler, updateContext: UpdateContext): Boolean {
        return updateContext.tgBotV2!!.supportedFeatures.containsAll(handler.handler.requiredFeatures())
    }

}