package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.logic.ChildStateTransitionProvider
import com.nikichxp.tgbot.core.service.tgapi.TgButton
import com.nikichxp.tgbot.core.service.tgapi.TgInlineKeyboard
import com.nikichxp.tgbot.core.service.tgapi.TgKeyboard
import com.nikichxp.tgbot.core.service.tgapi.TgReplyMarkup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

abstract class ChildKeyboardProvider {

    @Autowired
    lateinit var stateTransitionHelper: ChildStateTransitionProvider

    abstract fun getKeyboardForState(state: ChildActivity): TgReplyMarkup

    protected fun getItems(state: ChildActivity): List<String> {
        return stateTransitionHelper.getPossibleTransitions(state).values.toList()
    }

}

@Service
@Primary
class ReplyKeyboardProvider : ChildKeyboardProvider() {

    override fun getKeyboardForState(state: ChildActivity): TgKeyboard = TgKeyboard(
        keyboard = listOf(
            getItems(state).map { TgButton(it) }
        )
    )

}

@Service
class InlineKeyboardProvider : ChildKeyboardProvider() {

    override fun getKeyboardForState(state: ChildActivity): TgInlineKeyboard {
        TODO("Not yet implemented")
    }

}
