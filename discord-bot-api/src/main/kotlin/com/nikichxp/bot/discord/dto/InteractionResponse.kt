package com.nikichxp.bot.discord.dto

sealed class InteractionResponse {
    object Unauthenticated : InteractionResponse()
    object Pong : InteractionResponse()
    object Ok : InteractionResponse()
}
