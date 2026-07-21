package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.nikichxp.tgbot.core.dto.payments.PreCheckoutQuery
import com.nikichxp.tgbot.core.dto.payments.ShippingQuery
import com.nikichxp.tgbot.core.dto.polls.Poll
import com.nikichxp.tgbot.core.dto.polls.PollAnswer
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
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
    @JsonProperty("poll_answer") val pollAnswer: PollAnswer? = null,
    @JsonProperty("my_chat_member") val myChatMember: ChatMemberUpdated? = null,
    @JsonProperty("chat_member") val chatMember: ChatMemberUpdated? = null,
    @JsonProperty("chat_join_request") val chatJoinRequest: ChatJoinRequest? = null,
    @JsonProperty("message_reaction") val messageReaction: MessageReactionUpdated? = null,
    @JsonProperty("message_reaction_count") val messageReactionCount: MessageReactionCountUpdated? = null,
    @JsonProperty("chat_boost") val chatBoost: ChatBoostUpdated? = null,
    @JsonProperty("removed_chat_boost") val removedChatBoost: ChatBoostRemoved? = null,
    @JsonProperty("business_connection") val businessConnection: BusinessConnection? = null,
    @JsonProperty("business_message") val businessMessage: Message? = null,
    @JsonProperty("edited_business_message") val editedBusinessMessage: Message? = null,
    @JsonProperty("deleted_business_messages") val deletedBusinessMessages: BusinessMessagesDeleted? = null,
    @JsonProperty("purchased_paid_media") val purchasedPaidMedia: PaidMediaPurchased? = null
) {

    // TODO think about: maybe move this to updateContext, TBD
    @JsonIgnore
    lateinit var bot: TgBotInfo

}
