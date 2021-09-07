package com.nikichxp.tgbot.dto.dice

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents an animated emoji that displays a random value.
 * https://core.telegram.org/bots/api#dice
 */
data class Dice(
    @JsonProperty(DiceFields.EMOJI) val emoji: DiceEmoji,
    @JsonProperty(DiceFields.VALUE) val value: Int
)

sealed class DiceEmoji {
    abstract val emojiValue: String

    object Dice : DiceEmoji() {
        override val emojiValue: String
            get() = "🎲"
    }

    object Dartboard : DiceEmoji() {
        override val emojiValue: String
            get() = "🎯"
    }

    object Basketball : DiceEmoji() {
        override val emojiValue: String
            get() = "🏀"
    }

    object Football : DiceEmoji() {
        override val emojiValue: String
            get() = "⚽️"
    }

    object SlotMachine : DiceEmoji() {
        override val emojiValue: String
            get() = "🎰"
    }

    object Bowling : DiceEmoji() {
        override val emojiValue: String
            get() = "🎳"
    }

    // Currently not supported, adding it just in case Telegram Bot API
    // starts supporting new emojis for the dice in the future
    data class Other(override val emojiValue: String) : DiceEmoji()

    companion object {
        fun fromString(emoji: String): DiceEmoji = when (emoji) {
            Dice.emojiValue -> Dice
            Dartboard.emojiValue -> Dartboard
            Basketball.emojiValue -> Basketball
            Football.emojiValue -> Football
            SlotMachine.emojiValue -> SlotMachine
            Bowling.emojiValue -> Bowling
            else -> Other(emoji)
        }
    }
}
