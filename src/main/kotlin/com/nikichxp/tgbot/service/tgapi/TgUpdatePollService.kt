package com.nikichxp.tgbot.service.tgapi

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.BotInfo
import com.nikichxp.tgbot.service.MessageEntryPoint
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
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class TgUpdatePollService(
    private val client: HttpClient,
    @Lazy
    private val messageEntryPoint: MessageEntryPoint
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(dispatcher)
    private val activeBots = ConcurrentSet<PollingInfo>()

    fun startPollingFor(botInfo: BotInfo) {
        activeBots.add(PollingInfo(botInfo))
        logger.info("Start update polling for bot ${botInfo.name}")
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    fun pollData() {
        activeBots.map { info ->
            scope.launch {
                val response =
                    client.get("https://api.telegram.org/bot${info.bot.token}/getUpdates?offset=-100")
                        .body<TgResponse>()
                for (update in response.result.filter { info.shouldBeProcessed(it.updateId) }) {
                    messageEntryPoint.proceedUpdate(update, info.bot.bot)
                    info.process(update.updateId)
                }
            }
        }.map { runBlocking { it.join() } }
    }

}

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

    fun process(updateId: Long) {
        processedUpdates.add(updateId)
        while (processedUpdates.size > 1_000) {
            processedUpdates.poll()
        }
    }
}
