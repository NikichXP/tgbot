package com.nikichxp.tgbot.core.handlers.callbacks

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.handlers.BotSupportFeature

interface CallbackHandler : BotSupportFeature {

    fun isCallbackSupported(callbackContext: CallbackContext): Boolean
    fun handleCallback(callbackContext: CallbackContext, update: Update): Boolean

}