package com.nikichxp.tgbot.core.dto

import com.nikichxp.tgbot.core.dto.files.File

data class TgGetFileResponse(
    val ok: Boolean,
    val result: File?
)
