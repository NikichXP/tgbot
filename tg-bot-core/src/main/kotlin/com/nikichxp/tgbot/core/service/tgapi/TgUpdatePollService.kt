package com.nikichxp.tgbot.core.service.tgapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.service.MessageEntryPoint
import com.nikichxp.tgbot.core.service.tgapi.executor.ITgApiCallExecutor
import com.nikichxp.tgbot.core.service.tgapi.executor.TgResponseStatus
import io.ktor.util.collections.ConcurrentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class TgUpdatePollService(
    private val tgApiCallExecutor: ITgApiCallExecutor,
    private val objectMapper: ObjectMapper,
    private val tgBotWebhookService: TgBotWebhookService,
    @Lazy
    private val messageEntryPoint: MessageEntryPoint,
    private val tgLastKnownMessageService: TgLastKnownMessageService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(dispatcher)
    private val activePollingInfo = ConcurrentSet<PollingInfo>()

    fun startPollingFor(botInfo: TgBotInfo) {
        val lastKnownMessage = tgLastKnownMessageService.getLastKnownMessage(botInfo)
        val pollingInfo = mapToPollingInfo(botInfo, lastKnownMessage)
        activePollingInfo.add(pollingInfo)
        logger.info("Start update polling: $pollingInfo")
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    fun pollData() {
        val jobs = activePollingInfo.map { info ->
            scope.launch {
                val params = buildGetUpdatesParams(info)
                val rawResponse = tgApiCallExecutor.callEndpoint(info.bot, "getUpdates", params)
                when {
                    rawResponse.success -> {
                        val responseBody = objectMapper.treeToValue(rawResponse.content, TgResponse::class.java)
                        for (update in responseBody.result.filter { info.shouldBeProcessed(it.updateId) }) {
                            messageEntryPoint.proceedUpdate(update, info.bot)
                            info.onProcess(update.updateId)
                        }
                    }

                    rawResponse.responseStatus == TgResponseStatus.CONFLICT -> {
                        tgBotWebhookService.unregister(info.bot)
                    }

                    else -> {
                        logger.warn("Failed to fetch updates for ${info.bot.name}, error: ${rawResponse.content}")
                    }
                }
            }
        }
        runBlocking { jobs.joinAll() }
    }


    private fun mapToPollingInfo(botInfo: TgBotInfo, lastKnownMessage: BotLastKnownMessage): PollingInfo {
        return PollingInfo(botInfo, lastKnownMessage.updateId, lastKnownMessage.date)
    }

    private fun buildGetUpdatesParams(pollingInfo: PollingInfo): TgGetUpdatesParams {
        val offset = if (LocalDateTime.now().isBefore(pollingInfo.lastUpdateExpiryDate)) {
            pollingInfo.lastUpdate
        } else {
            null
        }
        return TgGetUpdatesParams(offset)
    }

}
