package com.nikichxp.tgbot.core.service.tgapi

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.bots.BotInfo
import com.nikichxp.tgbot.core.service.MessageEntryPoint
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
import org.springframework.data.mongodb.core.findById
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class TgUpdatePollService(
    private val client: HttpClient,
    @Lazy
    private val messageEntryPoint: MessageEntryPoint,
    @Lazy
    private val tgOperations: TgOperations,
    private val mongoTemplate: MongoTemplate
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(dispatcher)
    private val activePollingBots = ConcurrentSet<PollingInfo>()

    fun startPollingFor(botInfo: BotInfo) {
        val lastKnownMessage = mongoTemplate.findById<BotLastKnownMessage>(botInfo.bot.name)
        activePollingBots.add(PollingInfo(botInfo, lastKnownMessage?.updateId ?: 0))
        logger.info("Start update polling for bot ${botInfo.name}")
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    fun pollData() {


        activePollingBots.map { info ->
            scope.launch {
                val response =
                    client.get("https://api.telegram.org/bot${info.bot.token}/getUpdates?offset=${info.lastUpdate}")
                when (response.status.value) {
                    in 200..299 -> {
                        val responseBody = response.body<TgResponse>()
                        for (update in responseBody.result.filter { info.shouldBeProcessed(it.updateId) }) {
                            messageEntryPoint.proceedUpdate(update, info.bot.bot)
                            val hasUpdated = info.process(update.updateId)
                            if (hasUpdated) {
                                mongoTemplate.save(BotLastKnownMessage(info.bot.name, update.updateId))
                            }
                        }
                    }

                    409 -> {
//                        tgOperations.deleteWebhook(info.bot.bot)
                    }

                    else -> {
                        // todo logger of errors with limited rate
                    }
                }
            }
        }.map { runBlocking { it.join() } }
    }
}

data class BotLastKnownMessage(var id: String, var updateId: Long)

data class TgResponse(
    val ok: Boolean,
    val result: List<Update>
)

data class PollingInfo(
    val bot: BotInfo,
    var lastUpdate: Long = 0
) {
    // todo maybe consider using 2 structures?
    private val processedUpdates = LinkedList<Long>()

    fun shouldBeProcessed(updateId: Long): Boolean {
        return !processedUpdates.contains(updateId)
    }

    fun process(updateId: Long): Boolean {
        processedUpdates.add(updateId)
        while (processedUpdates.size > 1_000) {
            processedUpdates.poll()
        }
        if (updateId > lastUpdate) {
            lastUpdate = updateId
            return true
        }
        return false
    }
}
