package com.nikichxp.tgbot.core.dto.inlinequeryresults

import com.nikichxp.tgbot.core.dto.InlineKeyboardMarkup
import com.nikichxp.tgbot.core.dto.ParseMode
import com.fasterxml.jackson.annotation.JsonProperty

enum class MimeType(val rawName: String) {
    @JsonProperty("text/html")
    TEXT_HTML("text/html"),
    @JsonProperty("video/mp4")
    VIDEO_MP4("video/mp4"),
    @JsonProperty("application/pdf")
    APPLICATION_PDF("application/pdf"),
    @JsonProperty("application/zip")
    APPLICATION_ZIP("application/zip"),
    @JsonProperty("image/jpeg")
    IMAGE_JPEG("image/jpeg"),
    @JsonProperty("image/gif")
    IMAGE_GIF("image/gif")
}

fun String.toMimeType(): MimeType? =
    MimeType.values().firstOrNull { type -> this == type.rawName }

private object QueryResultTypes {
    const val ARTICLE = "article"
    const val PHOTO = "photo"
    const val VIDEO = "video"
    const val VOICE = "voice"
    const val STICKER = "sticker"
    const val MPEG4_GIF = "mpeg4_gif"
    const val GIF = "gif"
    const val AUDIO = "audio"
    const val DOCUMENT = "document"
    const val LOCATION = "location"
    const val VENUE = "venue"
    const val CONTACT = "contact"
    const val GAME = "game"
}

sealed class InlineQueryResult(
    val type: String
) {
    abstract val id: String
    abstract val replyMarkup: InlineKeyboardMarkup?

    data class Article(
        override val id: String,
        val title: String,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        val url: String? = null,
        @JsonProperty("hide_url") val hideUrl: Boolean? = null,
        val description: String? = null,
        @JsonProperty("thumb_url") val thumbUrl: String? = null,
        @JsonProperty("thumb_width") val thumbWidth: Int? = null,
        @JsonProperty("thumb_height") val thumbHeight: Int? = null
    ) : InlineQueryResult(QueryResultTypes.ARTICLE)

    data class Photo(
        override val id: String,
        @JsonProperty("photo_url") val photoUrl: String,
        @JsonProperty("thumb_url") val thumbUrl: String,
        @JsonProperty("photo_width") val photoWidth: Int? = null,
        @JsonProperty("photo_height") val photoHeight: Int? = null,
        val title: String? = null,
        val description: String? = null,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.PHOTO)

    data class Gif(
        override val id: String,
        @JsonProperty("gif_url") val gifUrl: String,
        @JsonProperty("gif_width") val gifWidth: Int? = null,
        @JsonProperty("gif_height") val gifHeight: Int? = null,
        @JsonProperty("gif_duration") val gifDuration: Int? = null,
        @JsonProperty("thumb_url") val thumbUrl: String,
        @JsonProperty("thumb_mime_type") val thumbMimeType: MimeType? = null,
        val title: String? = null,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.GIF)

    data class Mpeg4Gif(
        override val id: String,
        @JsonProperty("mpeg4_url") val mpeg4Url: String,
        @JsonProperty("mpeg4_width") val mpeg4Width: Int? = null,
        @JsonProperty("mpeg4_height") val mpeg4Height: Int? = null,
        @JsonProperty("mpeg4_duration") val mpeg4Duration: Int? = null,
        @JsonProperty("thumb_url") val thumbUrl: String,
        @JsonProperty("thumb_mime_type") val thumbMimeType: MimeType? = null,
        val title: String? = null,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.MPEG4_GIF)

    data class Video(
        override val id: String,
        @JsonProperty("video_url") val videoUrl: String,
        @JsonProperty("mime_type") val mimeType: MimeType,
        @JsonProperty("thumb_url") val thumbUrl: String,
        val title: String,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("video_width") val videoWidth: Int? = null,
        @JsonProperty("video_height") val videoHeight: Int? = null,
        @JsonProperty("video_duration") val videoDuration: Int? = null,
        val description: String? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.VIDEO)

    data class Audio(
        override val id: String,
        @JsonProperty("audio_url") val audioUrl: String,
        val title: String,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        val performer: String? = null,
        @JsonProperty("audio_duration") val audioDuration: Int? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.AUDIO)

    data class Voice(
        override val id: String,
        @JsonProperty("voice_url") val voiceUrl: String,
        val title: String,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("voice_duration") val voiceDuration: Int? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.VOICE)

    data class Document(
        override val id: String,
        val title: String,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("document_url") val documentUrl: String,
        @JsonProperty("mime_type") val mimeType: MimeType,
        val description: String? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null,
        @JsonProperty("thumb_url") val thumbUrl: String? = null,
        @JsonProperty("thumb_width") val thumbWidth: Int? = null,
        @JsonProperty("thumb_height") val thumbHeight: Int? = null
    ) : InlineQueryResult(QueryResultTypes.DOCUMENT)

    data class Location(
        override val id: String,
        val latitude: Float,
        val longitude: Float,
        val title: String,
        @JsonProperty("live_period") val livePeriod: Int? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null,
        @JsonProperty("thumb_url") val thumbUrl: String? = null,
        @JsonProperty("thumb_width") val thumbWidth: Int? = null,
        @JsonProperty("thumb_height") val thumbHeight: Int? = null
    ) : InlineQueryResult(QueryResultTypes.LOCATION)

    data class Venue(
        override val id: String,
        val latitude: Float,
        val longitude: Float,
        val title: String,
        val address: String,
        @JsonProperty("foursquare_id") val foursquareId: String? = null,
        @JsonProperty("foursquare_type") val foursquareType: String? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null,
        @JsonProperty("thumb_url") val thumbUrl: String? = null,
        @JsonProperty("thumb_width") val thumbWidth: Int? = null,
        @JsonProperty("thumb_height") val thumbHeight: Int? = null
    ) : InlineQueryResult(QueryResultTypes.VENUE)

    data class Contact(
        override val id: String,
        @JsonProperty("phone_number") val phoneNumber: String,
        @JsonProperty("first_name") val firstName: String,
        @JsonProperty("last_name") val lastName: String? = null,
        val vcard: String? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null,
        @JsonProperty("thumb_url") val thumbUrl: String? = null,
        @JsonProperty("thumb_width") val thumbWidth: Int? = null,
        @JsonProperty("thumb_height") val thumbHeight: Int? = null
    ) : InlineQueryResult(QueryResultTypes.CONTACT)

    data class Game(
        override val id: String,
        @JsonProperty("game_short_name") val gameShortName: String,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null
    ) : InlineQueryResult(QueryResultTypes.GAME)

    data class CachedAudio(
        override val id: String,
        @JsonProperty("audio_file_id") val audioFileId: String,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.AUDIO)

    data class CachedDocument(
        override val id: String,
        val title: String,
        @JsonProperty("document_file_id") val documentFileId: String,
        val description: String? = null,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.DOCUMENT)

    data class CachedGif(
        override val id: String,
        @JsonProperty("gif_file_id") val gifFileId: String,
        val title: String? = null,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.GIF)

    data class CachedMpeg4Gif(
        override val id: String,
        @JsonProperty("mpeg4_file_id") val mpeg4FileId: String,
        val title: String? = null,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.MPEG4_GIF)

    data class CachedPhoto(
        override val id: String,
        @JsonProperty("photo_file_id") val photoFileId: String,
        val title: String? = null,
        val description: String? = null,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.PHOTO)

    data class CachedSticker(
        override val id: String,
        @JsonProperty("sticker_file_id") val stickerFileId: String,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.STICKER)

    data class CachedVideo(
        override val id: String,
        @JsonProperty("video_file_id") val videoFileId: String,
        val title: String,
        val description: String? = null,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.VIDEO)

    data class CachedVoice(
        override val id: String,
        @JsonProperty("voice_file_id") val voiceFileId: String,
        val title: String,
        val caption: String? = null,
        @JsonProperty("parse_mode") val parseMode: ParseMode? = null,
        @JsonProperty("reply_markup") override val replyMarkup: InlineKeyboardMarkup? = null,
        @JsonProperty("input_message_content") val inputMessageContent: InputMessageContent? = null
    ) : InlineQueryResult(QueryResultTypes.VOICE)
}
