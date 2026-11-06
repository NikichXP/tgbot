package com.nikichxp.tgbot.core.service.tgapi

import com.nikichxp.bot.common.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.error.TgApiErrorException
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
        return try {
            val response = tgApiCallExecutor.callEndpoint(botInfo, "setWebhook", TgSetWebhookParams(webhookPath))
            logger.info(formatLog(botInfo, "Registered webhook, path: $webhookPath, message: ${response.content}"))
            true
        } catch (e: TgApiErrorException) {
            logger.info(formatLog(botInfo, "Failed to register webhook, path: $webhookPath, message: ${e.message}"))
            false
        }
    }

    suspend fun unregister(botInfo: TgBotInfo): Boolean {
        return try {
            val response = tgApiCallExecutor.callEndpoint(botInfo, "deleteWebhook", TgDeleteWebhookParams())
            logger.info(formatLog(botInfo, "Unregistered webhook with message: ${response.content}"))
            true
        } catch (e: TgApiErrorException) {
            logger.info(formatLog(botInfo, "Failed to unregister webhook with message: ${e.message}"))
            false
        }
    }

    private fun formatLog(botInfo: TgBotInfo, message: String): String = "Bot = ${botInfo.name}, message = $message"

}