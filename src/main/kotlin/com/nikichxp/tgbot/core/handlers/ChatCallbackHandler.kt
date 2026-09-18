package com.nikichxp.tgbot.core.handlers

import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackContext
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChatCallbackHandler(
    private val callbackHandlers: List<CallbackHandler>
) : UpdateHandler {

    private val log = LoggerFactory.getLogger(this::class.java)

    // TODO Do I need to have features like inline/callback?
    override fun requiredFeatures() = setOf<String>()

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.HAS_CALLBACK)

    override suspend fun handleUpdate(updateContext: UpdateContext) {
        val callbackContext = CallbackContext(updateContext)
        val result = callbackHandlers
            .filter { isRequiredFeatureSupported(it, updateContext) }
            .filter { if (it is Authenticable) it.authenticate(updateContext) else true }
            .find { it.isCallbackSupported(callbackContext) }
            ?.handleCallback(callbackContext)

        val status = when (result) {
            true -> "successfully handled"
            false -> "failed to handle"
            null -> "no handler found"
        }

        log.info("chadId = ${callbackContext.chatId} | $callbackContext | $status")
        // TODO maybe log all failed callbacks?
    }

    private fun isRequiredFeatureSupported(handler: CallbackHandler, updateContext: UpdateContext): Boolean {
        return updateContext.getBotInfo().getSupportedFeatures().containsAll(handler.requiredFeatures())
    }
}