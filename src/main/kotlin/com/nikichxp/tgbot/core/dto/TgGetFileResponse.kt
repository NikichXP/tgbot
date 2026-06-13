package com.nikichxp.tgbot.core.dto

import com.nikichxp.tgbot.core.dto.files.TgFile

data class TgGetFileResponse(
    val ok: Boolean,
    val result: TgFile?
)
