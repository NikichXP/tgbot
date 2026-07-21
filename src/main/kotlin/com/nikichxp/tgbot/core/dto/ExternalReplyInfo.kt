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
import com.nikichxp.tgbot.core.dto.polls.Poll
import com.nikichxp.tgbot.core.dto.stickers.Sticker

/**
 * Contains information about a message that is being replied to, which may come from another chat or forum topic.
 * https://core.telegram.org/bots/api#externalreplyinfo
 */
data class ExternalReplyInfo(
    val origin: MessageOrigin,
    val chat: Chat? = null,
    @JsonProperty("message_id") val messageId: Long? = null,
    @JsonProperty("link_preview_options") val linkPreviewOptions: LinkPreviewOptions? = null,
    val animation: Animation? = null,
    val audio: Audio? = null,
    val document: Document? = null,
    @JsonProperty("paid_media") val paidMedia: PaidMediaInfo? = null,
    val photo: List<PhotoSize>? = null,
    val sticker: Sticker? = null,
    val story: Story? = null,
    val video: Video? = null,
    @JsonProperty("video_note") val videoNote: VideoNote? = null,
    val voice: Voice? = null,
    @JsonProperty("has_media_spoiler") val hasMediaSpoiler: Boolean? = null,
    val contact: Contact? = null,
    val dice: Dice? = null,
    val game: Game? = null,
    val giveaway: Giveaway? = null,
    @JsonProperty("giveaway_winners") val giveawayWinners: GiveawayWinners? = null,
    val invoice: Invoice? = null,
    val location: Location? = null,
    val poll: Poll? = null,
    val venue: Venue? = null
)
