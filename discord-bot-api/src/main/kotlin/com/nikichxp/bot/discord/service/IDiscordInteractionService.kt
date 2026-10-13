package com.nikichxp.bot.discord.service

import com.nikichxp.bot.discord.dto.InteractionResponse

interface IDiscordInteractionService {
    suspend fun handleInteraction(
        signature: String?,
        timestamp: String?,
        bodyString: String,
        token: String
    ): InteractionResponse
}
