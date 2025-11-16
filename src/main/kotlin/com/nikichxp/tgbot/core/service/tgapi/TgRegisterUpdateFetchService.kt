package com.nikichxp.tgbot.core.service.tgapi

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.UpdateFetchType
import com.nikichxp.tgbot.core.service.TgBotV2Service
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TgRegisterUpdateFetchService(
    private val tgSetWebhookService: TgBotSetWebhookService,
    private val tgUpdatePollService: TgUpdatePollService,
    private val tgBotV2Service: TgBotV2Service,
    private val appConfig: AppConfig,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val bots = tgBotV2Service.listBots()

    @PostConstruct
    fun registerWebhooks() {
        if (appConfig.localEnv) {
            logger.info("Local environment is set to local - webhook will not be set")
        }

        tgBotV2Service.listBots().forEach { tgBotInfo ->
            when (tgBotInfo.updateFetchType) {
                UpdateFetchType.POLLING -> {
                    logger.info("Registering polling for bot: ${tgBotInfo.name}")
                    tgUpdatePollService.startPollingFor(tgBotInfo)
                }

                UpdateFetchType.WEBHOOK -> if (appConfig.localEnv || appConfig.suspendBotRegistering) {
                    logger.info("Local env: skip webhook setting for bot: ${tgBotInfo.name}")
                } else {
                    runBlocking { tgSetWebhookService.register(tgBotInfo) }
                }
            }
        }
    }
}
