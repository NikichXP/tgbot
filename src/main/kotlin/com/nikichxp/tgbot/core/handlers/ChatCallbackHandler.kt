package com.nikichxp.tgbot.core.handlers

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.entity.bots.TgBot
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackContext
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackHandler
import com.nikichxp.tgbot.core.util.getContextChatId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChatCallbackHandler(
    private val callbackHandlers: List<CallbackHandler>
) : UpdateHandler {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun supportedBots(): Set<TgBot> = TgBot.entries.toSet()
    override fun isBotSupported(tgBot: TgBot): Boolean = true

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.HAS_CALLBACK)

    override suspend fun handleUpdate(update: Update) {
        val callbackContext = CallbackContext(update)
        val result = callbackHandlers
            .filter { it.isBotSupported(update.bot) }
            .find { it.isCallbackSupported(callbackContext) }
            ?.handleCallback(callbackContext, update)

        val status = when (result) {
            true -> "successfully handled"
            false -> "failed to handle"
            null -> "no handler found"
        }

        log.info("chadId = ${update.getContextChatId()} | $callbackContext | $status")
        // TODO maybe log all failed callbacks?
    }
}