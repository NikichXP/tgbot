package com.nikichxp.bot.discord.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nikichxp.bot.discord.dto.DiscordInteraction
import com.nikichxp.bot.discord.dto.InteractionResponse
import com.nikichxp.bot.discord.repository.StoredJsonRepository
import org.springframework.stereotype.Service

@Service
class DiscordInteractionServiceImpl(
    private val signatureVerifier: DiscordSignatureVerifier,
    private val storedJsonRepository: StoredJsonRepository,
    private val objectMapper: ObjectMapper
) : IDiscordInteractionService {

    override suspend fun handleInteraction(
        signature: String?,
        timestamp: String?,
        bodyString: String,
        token: String
    ): InteractionResponse {
        if (!signatureVerifier.verifySignature(signature, timestamp, bodyString)) {
            return InteractionResponse.Unauthenticated
        }
        val interaction = objectMapper.readValue<DiscordInteraction>(bodyString)
        storedJsonRepository.saveJson(bodyString, token)
        return if (interaction.isPing()) {
            InteractionResponse.Pong
        } else {
            InteractionResponse.Ok
        }
    }
}
