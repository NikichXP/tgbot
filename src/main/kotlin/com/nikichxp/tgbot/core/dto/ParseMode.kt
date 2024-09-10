package com.nikichxp.tgbot.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

// TODO: Remove modeName attribute and stop using it as a serialization approach for this enum
enum class ParseMode(val modeName: String) {
    @JsonProperty("Markdown") MARKDOWN("Markdown"),
    @JsonProperty("HTML") HTML("HTML"),
    @JsonProperty("MarkdownV2") MARKDOWN_V2("MarkdownV2")
}
