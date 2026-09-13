package com.nikichxp.tgbot.core.entity.common

data class MessageModel(
    val id: Long,
    val text: String?,
    val voice: VoiceModel? = null,
    val sticker: StickerModel? = null
)

data class VoiceModel(
    val fileId: String,
    val fileUniqueId: String,
    val duration: Int,
    val mimeType: String?
)

data class StickerModel(
    val fileId: String,
    val emoji: String?
)
