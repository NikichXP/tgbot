package com.nikichxp.tgbot.summary

object RecapMessageSplitter {

    const val MAX_MESSAGE_LENGTH = 4000
    private const val PARAGRAPH_SEPARATOR = "\n\n"

    fun split(text: String, maxLength: Int = MAX_MESSAGE_LENGTH): List<String> {
        val paragraphs = text.split(Regex("\n\\s*\n"))
            .filter { it.isNotBlank() }
            .flatMap { splitBySentences(it, maxLength - 1) }

        val parts = mutableListOf<String>()
        val current = StringBuilder()
        for (paragraph in paragraphs) {
            if (current.isNotEmpty() && current.length + PARAGRAPH_SEPARATOR.length + paragraph.length >= maxLength) {
                parts += current.toString()
                current.clear()
            }
            if (current.isNotEmpty()) current.append(PARAGRAPH_SEPARATOR)
            current.append(paragraph)
        }
        if (current.isNotEmpty()) parts += current.toString()
        return parts
    }

    private fun splitBySentences(paragraph: String, maxLength: Int): List<String> {
        val parts = mutableListOf<String>()
        var rest = paragraph
        while (rest.length > maxLength) {
            val lastPeriod = rest.lastIndexOf('.', maxLength - 1)
            val cut = if (lastPeriod >= 0) lastPeriod + 1 else maxLength
            parts += rest.substring(0, cut)
            rest = rest.substring(cut).trimStart()
        }
        if (rest.isNotEmpty()) parts += rest
        return parts
    }
}
