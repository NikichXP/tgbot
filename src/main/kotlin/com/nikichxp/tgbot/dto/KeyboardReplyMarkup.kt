package com.nikichxp.tgbot.dto

import com.nikichxp.tgbot.dto.keyboard.KeyboardButton
import com.fasterxml.jackson.annotation.JsonProperty as Name

data class KeyboardReplyMarkup(
    val keyboard: List<List<KeyboardButton>>,
    @Name("resize_keyboard") val resizeKeyboard: Boolean = false,
    @Name("one_time_keyboard") val oneTimeKeyboard: Boolean = false,
    val selective: Boolean? = null
) : ReplyMarkup {

    constructor(
        vararg keyboard: KeyboardButton,
        resizeKeyboard: Boolean = false,
        oneTimeKeyboard: Boolean = false,
        selective: Boolean? = null
    ) : this(listOf(keyboard.toList()), resizeKeyboard, oneTimeKeyboard, selective)

    companion object {
        fun createSimpleKeyboard(
            keyboard: List<List<String>>,
            resizeKeyboard: Boolean = true,
            oneTimeKeyboard: Boolean = false,
            selective: Boolean? = null
        ): KeyboardReplyMarkup {
            return KeyboardReplyMarkup(keyboard.map { it.map { KeyboardButton(text = it) } }, resizeKeyboard, oneTimeKeyboard, selective)
        }
    }

}
