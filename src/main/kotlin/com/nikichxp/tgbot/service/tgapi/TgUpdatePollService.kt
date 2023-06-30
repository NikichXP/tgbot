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
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class TgUpdatePollService(
    private val client: HttpClient,
    @Lazy
    private val messageEntryPoint: MessageEntryPoint
) {

    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(dispatcher)
    private val activeBots = ConcurrentSet<PollingInfo>()

    fun startPollingFor(botInfo: BotInfo) {
        activeBots.add(PollingInfo(botInfo))
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    fun pollData() {
        activeBots.map { info ->
            scope.launch {
                val response =
                    client.get("https://api.telegram.org/bot${info.bot.token}/getUpdates?offset=${info.lastUpdate}")
                        .body<TgResponse>()
                for (update in response.result.filter { it.updateId > info.lastUpdate }) {
                    messageEntryPoint.proceedUpdate(update, info.bot.bot)
                }
                if (response.result.isNotEmpty()) {
                    info.lastUpdate = response.result.maxOf { it.updateId }
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
)