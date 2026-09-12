package com.nikichxp.tgbot.core.service.tgapi

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.service.tgapi.executor.ITgApiCallExecutor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TgBotWebhookService(
    private val tgApiCallExecutor: ITgApiCallExecutor,
    appConfig: AppConfig
) {

    private var webHookUrl = appConfig.webhook
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun register(botInfo: TgBotInfo): Boolean {
        val webhookPath = "$webHookUrl/${botInfo.name}"
        val response = tgApiCallExecutor.callEndpoint(botInfo, "setWebhook", TgSetWebhookParams(webhookPath))
        logger.info(
            formatLog(
                botInfo,
                "Register webhook status ${response.success}, path: $webhookPath, message: ${response.content}"
            )
        )
        return response.success
    }

    suspend fun unregister(botInfo: TgBotInfo): Boolean {
        val response = tgApiCallExecutor.callEndpoint(botInfo, "deleteWebhook", TgDeleteWebhookParams())
        logger.info(formatLog(botInfo, "Unregister webhook status ${response.success} with message: ${response.content}"))
        return response.success
    }

    private fun formatLog(botInfo: TgBotInfo, message: String): String = "Bot = ${botInfo.name}, message = $message"

}