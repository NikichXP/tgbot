package com.nikichxp.tgbot.core.service.tgapi

import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2
import com.nikichxp.tgbot.core.service.MessageEntryPoint
import com.nikichxp.tgbot.core.service.TgBotV2Service
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.util.collections.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class TgUpdatePollService(
    private val client: HttpClient,
    private val tgBotV2Service: TgBotV2Service,
    private val tgBotWebhookService: TgBotWebhookService,
    @Lazy
    private val messageEntryPoint: MessageEntryPoint,
    private val mongoTemplate: MongoTemplate,
    private val tgLastKnownMessageService: TgLastKnownMessageService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(dispatcher)
    private val activePollingInfo = ConcurrentSet<PollingInfo>()

    fun startPollingFor(botInfo: TgBotInfoV2) {
        val lastKnownMessage = tgLastKnownMessageService.getLastKnownMessage(botInfo)
        val pollingInfo = mapToPollingInfo(botInfo, lastKnownMessage)
        activePollingInfo.add(pollingInfo)
        logger.info("Start update polling: $pollingInfo")
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    fun pollData() {
        activePollingInfo.map { info ->
            scope.launch {
                val url = getUpdatesUrl(info)
                val response = client.get(url)
                when (response.status.value) {
                    in 200..299 -> {
                        val responseBody = response.body<TgResponse>()
                        for (update in responseBody.result.filter { info.shouldBeProcessed(it.updateId) }) {
                            messageEntryPoint.proceedUpdate(update, info.bot)
                            info.onProcess(update.updateId)
                        }
                    }

                    409 -> {
                        tgBotWebhookService.unregister(info.bot)
                    }

                    else -> {
                        logger.warn("Failed to fetch updates for ${info.bot.name}, unexpected error ${response.status.value}")
                    }
                }
            }
        }.map { runBlocking { it.join() } }
    }


    private fun mapToPollingInfo(botInfo: TgBotInfoV2, lastKnownMessage: BotLastKnownMessage): PollingInfo {
        val token = tgBotV2Service.getTokenById(botInfo.name)
        return PollingInfo(botInfo, lastKnownMessage.updateId, lastKnownMessage.date)
            .also { it.token = token }
    }

    private fun getUpdatesUrl(pollingInfo: PollingInfo): String {
        val updateSeq = if (LocalDateTime.now().isBefore(pollingInfo.lastUpdateExpiryDate)) {
            "?offset=${pollingInfo.lastUpdate}"
        } else {
            ""
        }
        return "https://api.telegram.org/bot${pollingInfo.token}/getUpdates$updateSeq"
    }

}
