package com.nikichxp.tgbot.core.handlers

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChatCallbackHandler : UpdateHandler {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun botSupported(bot: TgBot) = true

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.HAS_CALLBACK)

    override suspend fun handleUpdate(update: Update) {
        val callbackContext = CallbackContext(update)
        log.info("WOOOOOW, CALLBACK! ~_~ $callbackContext")
    }
}