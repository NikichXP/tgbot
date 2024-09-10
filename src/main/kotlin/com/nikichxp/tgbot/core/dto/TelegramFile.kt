package com.nikichxp.tgbot.core.dto

import java.io.File

sealed class TelegramFile {
    data class ByFileId(val fileId: String) : TelegramFile()
    data class ByUrl(val url: String) : TelegramFile()
    data class ByFile(val file: File) : TelegramFile()
}
