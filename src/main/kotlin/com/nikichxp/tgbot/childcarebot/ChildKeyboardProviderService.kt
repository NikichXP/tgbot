package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.logic.ChildStateTransitionHelper
import com.nikichxp.tgbot.core.service.tgapi.TgInlineKeyboard
import com.nikichxp.tgbot.core.service.tgapi.TgKeyboard
import com.nikichxp.tgbot.core.service.tgapi.TgReplyMarkup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

abstract class ChildKeyboardProviderService {

    @Autowired
    lateinit var stateTransitionHelper: ChildStateTransitionHelper

    abstract fun getKeyboardForState(state: ChildActivity): TgReplyMarkup


}

@Service
@Primary
class ReplyKeyboardProviderService : ChildKeyboardProviderService() {

    override fun getKeyboardForState(state: ChildActivity): TgKeyboard {
        TODO("Not yet implemented")
    }

}

@Service
@ConditionalOnMissingBean(ReplyKeyboardProviderService::class)
class InlineKeyboardProviderService : ChildKeyboardProviderService() {

    override fun getKeyboardForState(state: ChildActivity): TgInlineKeyboard {
        TODO("Not yet implemented")
    }

}
