package com.nikichxp.tgbot.core.service.tgapi

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.entity.bots.TgUpdateFetchType
import com.nikichxp.tgbot.core.service.ITgBotV2Service
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TgRegisterUpdateFetchService(
    private val tgBotWebhookService: TgBotWebhookService,
    private val tgUpdatePollService: TgUpdatePollService,
    private val tgBotV2Service: ITgBotV2Service,
    private val appConfig: AppConfig,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun registerWebhooks() {
        if (appConfig.localEnv) {
            logger.info("Local environment is set to local - webhook will not be set")
        }

        runBlocking {
            tgBotV2Service.listBots().forEach { registerBot(it) }
        }
    }

    suspend fun registerBot(tgBotInfo: TgBotInfo) {
        when (tgBotInfo.updateFetchType) {
            TgUpdateFetchType.POLLING -> {
                logger.info("Registering polling for bot: ${tgBotInfo.name}")
                tgUpdatePollService.startPollingFor(tgBotInfo)
            }

            TgUpdateFetchType.WEBHOOK -> if (appConfig.localEnv || appConfig.suspendBotRegistering) {
                logger.info("Local env: skip webhook setting for bot: ${tgBotInfo.name}")
            } else {
                tgBotWebhookService.register(tgBotInfo)
            }
        }
    }
}
