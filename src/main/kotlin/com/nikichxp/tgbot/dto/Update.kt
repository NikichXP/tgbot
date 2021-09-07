package com.nikichxp.tgbot.dto

import com.nikichxp.tgbot.dto.payments.PreCheckoutQuery
import com.nikichxp.tgbot.dto.payments.ShippingQuery
import com.nikichxp.tgbot.dto.polls.Poll
import com.nikichxp.tgbot.dto.polls.PollAnswer
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id

data class Update(
    @Id
    @JsonProperty("update_id") val updateId: Long,
    val message: Message? = null,
    @JsonProperty("edited_message") val editedMessage: Message? = null,
    @JsonProperty("channel_post") val channelPost: Message? = null,
    @JsonProperty("edited_channel_post") val editedChannelPost: Message? = null,
    @JsonProperty("inline_query") val inlineQuery: InlineQuery? = null,
    @JsonProperty("chosen_inline_result") val chosenInlineResult: ChosenInlineResult? = null,
    @JsonProperty("callback_query") val callbackQuery: CallbackQuery? = null,
    @JsonProperty("shipping_query") val shippingQuery: ShippingQuery? = null,
    @JsonProperty("pre_checkout_query") val preCheckoutQuery: PreCheckoutQuery? = null,
    @JsonProperty("poll") val poll: Poll? = null,
    @JsonProperty("poll_answer") val pollAnswer: PollAnswer? = null
)

/**
 * Generate list of key-value from start payload.
 * For more info {@link https://core.telegram.org/bots#deep-linking}
 */
fun Update.getStartPayload(delimiter: String = "-"): List<Pair<String, String>> {
    return message?.let {
        val parameters = it.text?.substringAfter("start ", "")
        if (parameters == null || parameters.isEmpty()) {
            return emptyList()
        }

        val split = parameters.split("&")
        split.map {
            val keyValue = it.split(delimiter)
            Pair(keyValue[0], keyValue[1])
        }
    } ?: emptyList()
}
