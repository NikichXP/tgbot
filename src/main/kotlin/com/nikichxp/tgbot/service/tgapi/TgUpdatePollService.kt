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
    private val activeBots = ConcurrentSet<BotInfo>()

    fun startPollingFor(botInfo: BotInfo) {
        activeBots.add(botInfo)
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    fun pollData() {
        activeBots.map { bot ->
            scope.launch {
                val response = client.get("https://api.telegram.org/bot${bot.token}/getUpdates").body<TgResponse>()
                for (update in response.result) {
                    // TODO local thread is not an HTTP thread, that's sad
                    messageEntryPoint.proceedUpdate(update, bot.bot)
                }
            }
        }.map { runBlocking { it.join() } }
    }

}

data class TgResponse(
    val ok: Boolean,
    val result: List<Update>
)