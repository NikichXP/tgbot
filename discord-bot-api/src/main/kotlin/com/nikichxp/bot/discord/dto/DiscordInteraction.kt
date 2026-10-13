package com.nikichxp.bot.discord.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DiscordInteraction(
    val type: Int
) {

    fun isPing(): Boolean = type == PING_TYPE

    private companion object {
        const val PING_TYPE = 1
    }
}
