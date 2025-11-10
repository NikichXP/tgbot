package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.nikichxp.tgbot.core.dto.dice.Dice
import com.nikichxp.tgbot.core.dto.files.Animation
import com.nikichxp.tgbot.core.dto.files.Audio
import com.nikichxp.tgbot.core.dto.files.Document
import com.nikichxp.tgbot.core.dto.files.PhotoSize
import com.nikichxp.tgbot.core.dto.files.Video
import com.nikichxp.tgbot.core.dto.files.VideoNote
import com.nikichxp.tgbot.core.dto.files.Voice
import com.nikichxp.tgbot.core.dto.payments.SuccessfulPayment
import com.nikichxp.tgbot.core.dto.polls.Poll
import com.nikichxp.tgbot.core.dto.stickers.Sticker

data class Message(
    @JsonProperty("message_id") val messageId: Long,
    val from: User? = null,
    @JsonProperty("sender_chat") val senderChat: Chat? = null,
    val date: Long,
    val chat: Chat,
    @JsonProperty("forward_from") val forwardFrom: User? = null,
    @JsonProperty("forward_from_chat") val forwardFromChat: Chat? = null,
    @JsonProperty("forward_from_message_id") val forwardFromMessageId: Int? = null,
    @JsonProperty("forward_signature") val forwardSignature: String? = null,
    @JsonProperty("forward_sender_name") val forwardSenderName: String? = null,
    @JsonProperty("forward_date") val forwardDate: Int? = null,
    @JsonProperty("reply_to_message") val replyToMessage: Message? = null,
    @JsonProperty("via_bot") val viaBot: User? = null,
    @JsonProperty("edit_date") val editDate: Int? = null,
    @JsonProperty("media_group_id") val mediaGroupId: String? = null,
    @JsonProperty("author_signature") val authorSignature: String? = null,
    val text: String? = null,
    val entities: List<MessageEntity>? = null,
    @JsonProperty("caption_entities") val captionEntities: List<MessageEntity>? = null,
    val audio: Audio? = null,
    val document: Document? = null,
    val animation: Animation? = null,
    @JsonProperty("dice") val dice: Dice? = null,
    val game: Game? = null,
    val photo: List<PhotoSize>? = null,
    val sticker: Sticker? = null,
    val video: Video? = null,
    val voice: Voice? = null,
    @JsonProperty("video_note") val videoNote: VideoNote? = null,
    val caption: String? = null,
    val contact: Contact? = null,
    val location: Location? = null,
    val venue: Venue? = null,
    @JsonProperty("new_chat_members") val newChatMembers: List<User>? = null,
    val poll: Poll? = null,
    @JsonProperty("left_chat_member") val leftChatMember: User? = null,
    @JsonProperty("new_chat_title") val newChatTitle: String? = null,
    @JsonProperty("new_chat_photo") val newChatPhoto: List<PhotoSize>? = null,
    @JsonProperty("delete_chat_photo") val deleteChatPhoto: Boolean? = null,
    @JsonProperty("group_chat_created") val groupChatCreated: Boolean? = null,
    @JsonProperty("supergroup_chat_created") val supergroupChatCreated: Boolean? = null,
    @JsonProperty("channel_chat_created") val channelChatCreated: Boolean? = null,
    @JsonProperty("migrate_to_chat_id") val migrateToChatId: Long? = null,
    @JsonProperty("migrate_from_chat_id") val migrateFromChatId: Long? = null,
    val invoice: Invoice? = null,
    @JsonProperty("successful_payment") val successfulPayment: SuccessfulPayment? = null,
    @JsonProperty("reply_markup") val replyMarkup: InlineKeyboardMarkup? = null
)
