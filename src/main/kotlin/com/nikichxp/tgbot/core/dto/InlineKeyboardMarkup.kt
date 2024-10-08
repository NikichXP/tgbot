package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.nikichxp.tgbot.core.dto.keyboard.InlineKeyboardButton

/**
 * This object represents an inline keyboard that appears right next to the message it belongs to.
 * @param inlineKeyboard Array of button rows, each represented by an Array of [InlineKeyboardButton] objects.
 * @see <https://core.telegram.org/bots/api#inlinekeyboardmarkup>
 */
data class InlineKeyboardMarkup internal constructor(
    @JsonProperty("inline_keyboard") val inlineKeyboard: List<List<InlineKeyboardButton>>
) : ReplyMarkup {

    init {
        validatePriorityButtonsForType<InlineKeyboardButton.Pay>()
        validatePriorityButtonsForType<InlineKeyboardButton.CallbackGameButtonType>()
    }

    // Priority buttons must always be the first button in the first row.
    private inline fun <reified T> validatePriorityButtonsForType() {
        val flattenedButtons = inlineKeyboard.flatten()
        val filteredButtons = flattenedButtons.filterIsInstance<T>()
        val typeName = T::class.simpleName

        require(filteredButtons.size <= 1) {
            "Can't have more than one button of type $typeName per inline keyboard"
        }

        require(filteredButtons.size != 1 || flattenedButtons.firstOrNull() is T) {
            "Buttons of type $typeName must always be the first button in the first row"
        }
    }

    companion object {

        fun createSingleButton(button: InlineKeyboardButton) = InlineKeyboardMarkup(listOf(listOf(button)))
        fun createSingleRowKeyboard(buttons: List<InlineKeyboardButton>) = InlineKeyboardMarkup(listOf(buttons))
        fun createSingleRowKeyboard(vararg button: InlineKeyboardButton) = InlineKeyboardMarkup(listOf(button.toList()))
        fun create(buttons: List<List<InlineKeyboardButton>>) = InlineKeyboardMarkup(buttons)
        fun create(vararg buttonsRow: List<InlineKeyboardButton>) = InlineKeyboardMarkup(buttonsRow.toList())
    }
}
