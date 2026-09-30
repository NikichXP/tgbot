package com.nikichxp.tgbot.dashboard.service

import com.nikichxp.tgbot.core.util.SecureTokens
import com.nikichxp.tgbot.dashboard.dto.CreateOAuthClientRequest
import com.nikichxp.tgbot.dashboard.dto.OAuthClientDto
import com.nikichxp.tgbot.dashboard.dto.OAuthClientWithSecretDto
import com.nikichxp.tgbot.dashboard.error.DashboardConflictException
import com.nikichxp.tgbot.dashboard.error.DashboardNotFoundException
import com.nikichxp.tgbot.oauth.entity.OAuthClient
import com.nikichxp.tgbot.oauth.repository.OAuthClientRepository
import com.nikichxp.tgbot.oauth.service.RedirectUriPolicy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class DashboardOAuthClientService(
    private val clientRepository: OAuthClientRepository
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val clientIdRegex = Regex("^[a-z0-9][a-z0-9_-]{0,63}$")

    suspend fun listClients(): List<OAuthClientDto> =
        clientRepository.findAll().map { it.toDto() }.sortedBy { it.clientId }

    suspend fun createClient(request: CreateOAuthClientRequest): OAuthClientWithSecretDto {
        val clientId = request.clientId.trim()
        require(clientIdRegex.matches(clientId)) {
            "Client id may contain only lowercase letters, digits, '_' and '-' (up to 64 chars)"
        }
        val redirectUris = request.redirectUris.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        require(redirectUris.isNotEmpty()) { "At least one redirect URI is required" }
        redirectUris.forEach(RedirectUriPolicy::requireValid)
        if (clientRepository.findById(clientId) != null) {
            throw DashboardConflictException("OAuth client '$clientId' already exists")
        }

        val secret = SecureTokens.randomUrlSafe()
        val client = clientRepository.insert(
            OAuthClient(
                clientId = clientId,
                name = request.name?.trim()?.takeIf { it.isNotEmpty() } ?: clientId,
                secretHash = SecureTokens.sha256Hex(secret),
                redirectUris = redirectUris,
                createdAt = Instant.now()
            )
        )
        logger.info("Dashboard: added OAuth client $clientId, redirectUris=$redirectUris")
        return OAuthClientWithSecretDto(client.toDto(), secret)
    }

    suspend fun rotateSecret(clientId: String): OAuthClientWithSecretDto {
        val client = clientRepository.findById(clientId)
            ?: throw DashboardNotFoundException("OAuth client '$clientId' not found")
        val secret = SecureTokens.randomUrlSafe()
        val updated = clientRepository.save(client.copy(secretHash = SecureTokens.sha256Hex(secret)))
        logger.info("Dashboard: rotated secret of OAuth client $clientId")
        return OAuthClientWithSecretDto(updated.toDto(), secret)
    }

    suspend fun deleteClient(clientId: String) {
        if (!clientRepository.deleteById(clientId)) {
            throw DashboardNotFoundException("OAuth client '$clientId' not found")
        }
        logger.info("Dashboard: deleted OAuth client $clientId")
    }

    private fun OAuthClient.toDto() = OAuthClientDto(clientId, name, redirectUris, createdAt)
}
