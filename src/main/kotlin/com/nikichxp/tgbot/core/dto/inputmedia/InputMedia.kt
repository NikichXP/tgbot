package com.nikichxp.tgbot.core.dto.inputmedia

import com.fasterxml.jackson.annotation.JsonProperty
import com.nikichxp.tgbot.core.dto.TelegramFile

/**
 * Represents the content of a media message to be sent.
 * https://core.telegram.org/bots/api#inputmedia
 */
sealed class InputMedia {
    abstract val type: String
    abstract val media: TelegramFile
    abstract val caption: String?
    abstract val parseMode: String?
}

/**
 * Interface to mark all the media types that can be sent within a group of media for
 * operations like `sendMediaGroup`.
 */
interface GroupableMedia

class MediaGroup private constructor(val medias: Array<out GroupableMedia>) {
    init {
        if (!(2..10).contains(medias.size)) {
            throw IllegalArgumentException("media groups must include 2-10 items")
        }
    }

    companion object {
        fun from(vararg media: GroupableMedia): MediaGroup = MediaGroup(media)
    }
}

/**
 * Represents a photo to be sent. Can be sent as part of a group of media.
 * https://core.telegram.org/bots/api#inputmediaphoto
 */
data class InputMediaPhoto(
    @JsonProperty(InputMediaFields.MEDIA) override val media: TelegramFile,
    @JsonProperty(InputMediaFields.CAPTION) override val caption: String? = null,
    @JsonProperty(InputMediaFields.PARSE_MODE) override val parseMode: String? = null
) : InputMedia(), GroupableMedia {
    override val type: String
        get() = InputMediaTypes.PHOTO
}

/**
 * Represents a video to be sent. Can be sent as part of a group of media.
 * https://core.telegram.org/bots/api#inputmediavideo
 */
data class InputMediaVideo(
    @JsonProperty(InputMediaFields.MEDIA) override val media: TelegramFile,
    @JsonProperty(InputMediaFields.CAPTION) override val caption: String? = null,
    @JsonProperty(InputMediaFields.PARSE_MODE) override val parseMode: String? = null,
    @JsonProperty(InputMediaFields.THUMB) val thumb: TelegramFile.ByFile? = null,
    @JsonProperty(InputMediaFields.WIDTH) val width: Int? = null,
    @JsonProperty(InputMediaFields.HEIGHT) val height: Int? = null,
    @JsonProperty(InputMediaFields.DURATION) val duration: Int? = null,
    @JsonProperty(InputMediaFields.SUPPORTS_STREAMING) val supportsStreaming: Boolean? = null
) : InputMedia(), GroupableMedia {
    override val type: String
        get() = InputMediaTypes.VIDEO
}

/**
 * Represents an animation file (GIF or H.264/MPEG-4 AVC video without sound) to be sent.
 * https://core.telegram.org/bots/api#inputmediaanimation
 */
data class InputMediaAnimation(
    @JsonProperty(InputMediaFields.MEDIA) override val media: TelegramFile,
    @JsonProperty(InputMediaFields.CAPTION) override val caption: String? = null,
    @JsonProperty(InputMediaFields.PARSE_MODE) override val parseMode: String? = null,
    @JsonProperty(InputMediaFields.THUMB) val thumb: TelegramFile.ByFile? = null,
    @JsonProperty(InputMediaFields.WIDTH) val width: Int? = null,
    @JsonProperty(InputMediaFields.HEIGHT) val height: Int? = null,
    @JsonProperty(InputMediaFields.DURATION) val duration: Int? = null
) : InputMedia() {
    override val type: String
        get() = InputMediaTypes.ANIMATION
}

/**
 * Represents an audio file to be treated as music to be sent.
 * https://core.telegram.org/bots/api#inputmediaaudio
 */
data class InputMediaAudio(
    @JsonProperty(InputMediaFields.MEDIA) override val media: TelegramFile,
    @JsonProperty(InputMediaFields.CAPTION) override val caption: String? = null,
    @JsonProperty(InputMediaFields.PARSE_MODE) override val parseMode: String? = null,
    @JsonProperty(InputMediaFields.THUMB) val thumb: TelegramFile.ByFile? = null,
    @JsonProperty(InputMediaFields.DURATION) val duration: Int? = null,
    @JsonProperty(InputMediaFields.PERFORMER) val performer: String? = null,
    @JsonProperty(InputMediaFields.TITLE) val title: String? = null
) : InputMedia(), GroupableMedia {
    override val type: String
        get() = InputMediaTypes.AUDIO
}

/**
 * Represents a general file to be sent.
 * https://core.telegram.org/bots/api#inputmediadocument
 */
data class InputMediaDocument(
    @JsonProperty(InputMediaFields.MEDIA) override val media: TelegramFile,
    @JsonProperty(InputMediaFields.CAPTION) override val caption: String? = null,
    @JsonProperty(InputMediaFields.PARSE_MODE) override val parseMode: String? = null,
    @JsonProperty(InputMediaFields.THUMB) val thumb: TelegramFile.ByFile? = null
) : InputMedia(), GroupableMedia {
    override val type: String
        get() = InputMediaTypes.DOCUMENT
}
