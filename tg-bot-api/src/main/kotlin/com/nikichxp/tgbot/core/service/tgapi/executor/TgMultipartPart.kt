package com.nikichxp.tgbot.core.service.tgapi.executor

sealed interface TgMultipartPart {
    data class Text(val name: String, val value: String) : TgMultipartPart
    class FilePart(
        val name: String,
        val fileName: String,
        val contentType: String,
        val content: ByteArray
    ) : TgMultipartPart {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is FilePart) return false
            return name == other.name &&
                fileName == other.fileName &&
                contentType == other.contentType &&
                content.size == other.content.size
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + fileName.hashCode()
            result = 31 * result + contentType.hashCode()
            result = 31 * result + content.size
            return result
        }

        override fun toString(): String {
            return "FilePart(name='$name', fileName='$fileName', contentType='$contentType', contentSize=${content.size})"
        }
    }
}
