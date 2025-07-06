package com.nikichxp.tgbot.core.service.tgapi

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.TgBotProvider
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class TgRegisterWebhookService(
    private val tgSetWebhookService: TgBotSetWebhookService,
    private val tgUpdatePollService: TgUpdatePollService,
    private val tgBotProvider: TgBotProvider,
    private val appConfig: AppConfig,
) {

    private val bots = tgBotProvider.getInitializedBots()
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun registerWebhooks() {
        if (appConfig.localEnv || appConfig.suspendBotRegistering) {
            logger.info("Local env: skip webhook setting")
            bots.forEach { tgUpdatePollService.startPollingFor(it) }
        } else {
            logger.info("Registering bots: ${bots.map { it.bot }}")
            bots.forEach {
                val webhookSet = runBlocking { tgSetWebhookService.register(it) }
                if (!webhookSet) {
                    logger.warn("Webhook setting failed for bot: ${it.bot}, doing polling instead")
                    tgUpdatePollService.startPollingFor(it)
                }
            }
        }
    }
}
