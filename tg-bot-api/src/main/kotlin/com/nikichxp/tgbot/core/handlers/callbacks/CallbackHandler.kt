package com.nikichxp.tgbot.core.handlers.callbacks

import com.nikichxp.bot.common.handlers.BotSupportFeature

interface CallbackHandler : BotSupportFeature {

    fun isCallbackSupported(callbackContext: CallbackContext): Boolean
    suspend fun handleCallback(callbackContext: CallbackContext): Boolean

}