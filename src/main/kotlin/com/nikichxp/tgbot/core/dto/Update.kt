package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nikichxp.tgbot.core.dto.payments.PreCheckoutQuery
import com.nikichxp.tgbot.core.dto.payments.ShippingQuery
import com.nikichxp.tgbot.core.dto.polls.Poll
import com.nikichxp.tgbot.core.dto.polls.PollAnswer
import com.nikichxp.tgbot.core.entity.BotInfo
import com.nikichxp.tgbot.core.entity.TgBot
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
) {

    // TODO think about: maybe move this to updateContext, TBD
    @JsonIgnore
    lateinit var bot: TgBot

}
