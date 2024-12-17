package com.nikichxp.tgbot.core.handlers.callbacks

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot

interface CallbackHandler {

    fun supportedBotsCallbacks(): Set<TgBot>
    fun isCallbackSupported(callbackContext: CallbackContext): Boolean
    fun handleCallback(callbackContext: CallbackContext, update: Update): Boolean

}