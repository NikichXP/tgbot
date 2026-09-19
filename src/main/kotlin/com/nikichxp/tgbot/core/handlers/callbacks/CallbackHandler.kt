package com.nikichxp.tgbot.core.handlers.callbacks

import com.nikichxp.tgbot.core.handlers.BotSupportFeature

interface CallbackHandler : BotSupportFeature {

    fun isCallbackSupported(callbackContext: CallbackContext): Boolean
    suspend fun handleCallback(callbackContext: CallbackContext): Boolean

}