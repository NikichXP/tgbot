package com.nikichxp.tgbot.oauth.dto

import com.fasterxml.jackson.annotation.JsonValue

enum class OAuthResponseMode(@get:JsonValue val wireName: String) {
    QUERY("query"),
    WEB_MESSAGE("web_message");

    companion object {
        fun fromWireNameOrDefault(wireName: String?): OAuthResponseMode? =
            if (wireName.isNullOrBlank()) QUERY else entries.find { it.wireName == wireName }
    }
}
