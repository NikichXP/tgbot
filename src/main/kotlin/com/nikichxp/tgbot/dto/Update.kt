package com.nikichxp.tgbot.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.nikichxp.tgbot.dto.payments.PreCheckoutQuery
import com.nikichxp.tgbot.dto.payments.ShippingQuery
import com.nikichxp.tgbot.dto.polls.Poll
import com.nikichxp.tgbot.dto.polls.PollAnswer
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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

    fun toJson(): String {
        return ObjectMapper().registerKotlinModule().writeValueAsString(this)
    }

    @JsonIgnore
    fun getContextChatId(): Long? {
        fun getChatId(message: Message?) = message?.chat?.id
        return getChatId(this.message)
            ?: getChatId(this.editedMessage)
            ?: getChatId(this.editedChannelPost)
            ?: getChatId(this.channelPost)
            ?: getChatId(this.callbackQuery?.message)
    }

}
