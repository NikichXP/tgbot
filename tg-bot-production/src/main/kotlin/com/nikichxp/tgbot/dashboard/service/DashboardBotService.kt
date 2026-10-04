package com.nikichxp.tgbot.dashboard.service

import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2Entity
import com.nikichxp.tgbot.core.service.IFeatureRegistry
import com.nikichxp.tgbot.core.service.ITgBotV2Service
import com.nikichxp.tgbot.core.service.tgapi.TgRegisterUpdateFetchService
import com.nikichxp.tgbot.dashboard.connector.TelegramBotApiClient
import com.nikichxp.tgbot.dashboard.dto.CreateBotRequest
import com.nikichxp.tgbot.dashboard.dto.DashboardBotDto
import com.nikichxp.tgbot.dashboard.dto.DashboardFeatureDto
import com.nikichxp.tgbot.dashboard.dto.UpdateBotFeaturesRequest
import com.nikichxp.tgbot.dashboard.error.DashboardConflictException
import com.nikichxp.tgbot.dashboard.error.DashboardNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service

@Service
class DashboardBotService(
    private val tgBotV2Service: ITgBotV2Service,
    private val tgRegisterUpdateFetchService: TgRegisterUpdateFetchService,
    private val telegramBotApiClient: TelegramBotApiClient,
    private val featureRegistry: IFeatureRegistry,
    private val conversionService: ConversionService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val botNameRegex = Regex("^[A-Za-z0-9_-]{1,64}$")

    fun availableFeatures(): List<DashboardFeatureDto> =
        featureRegistry.allFeatures().map { conversionService.convert(it, DashboardFeatureDto::class.java)!! }

    suspend fun listBots(): List<DashboardBotDto> = withContext(Dispatchers.IO) {
        tgBotV2Service.listBotEntities().map { it.toDto() }.sortedBy { it.name }
    }

    suspend fun createBot(request: CreateBotRequest): DashboardBotDto {
        val token = request.token.trim()
        require(token.isNotEmpty()) { "Token is required" }
        val identity = telegramBotApiClient.getMe(token)
            ?: throw IllegalArgumentException("Telegram rejected the token")

        val name = request.name?.trim()?.takeIf { it.isNotEmpty() } ?: identity.username
        require(botNameRegex.matches(name)) { "Bot name may contain only letters, digits, '_' and '-'" }
        validateFeatures(request.supportedFeatures, allowed = featureRegistry.featureIds())

        val entity = withContext(Dispatchers.IO) {
            if (tgBotV2Service.findBotEntity(name) != null) {
                throw DashboardConflictException("Bot '$name' already exists")
            }
            tgBotV2Service.saveBotEntity(TgBotInfoV2Entity(name).apply {
                this.token = token
                this.updateFetchType = request.updateFetchType
                this.supportedFeatures = request.supportedFeatures
            })
        }
        logger.info("Dashboard: added bot $name (@${identity.username}), features=${entity.supportedFeatures}")

        tgRegisterUpdateFetchService.registerBot(TgBotInfo(entity))
        return entity.toDto()
    }

    suspend fun updateFeatures(name: String, request: UpdateBotFeaturesRequest): DashboardBotDto =
        withContext(Dispatchers.IO) {
            val entity = tgBotV2Service.findBotEntity(name)
                ?: throw DashboardNotFoundException("Bot '$name' not found")
            validateFeatures(request.supportedFeatures, allowed = featureRegistry.featureIds() + entity.supportedFeatures)
            entity.supportedFeatures = request.supportedFeatures
            logger.info("Dashboard: bot $name features set to ${entity.supportedFeatures}")
            tgBotV2Service.saveBotEntity(entity).toDto()
        }

    private fun validateFeatures(features: Set<String>, allowed: Set<String>) {
        val unknown = features - allowed
        require(unknown.isEmpty()) { "Unknown features: ${unknown.joinToString()}" }
    }

    private fun TgBotInfoV2Entity.toDto() = DashboardBotDto(name, updateFetchType, supportedFeatures)
}
