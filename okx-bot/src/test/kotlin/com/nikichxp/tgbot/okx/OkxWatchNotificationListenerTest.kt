package com.nikichxp.tgbot.okx

import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.error.TgForbiddenException
import com.nikichxp.tgbot.core.service.ITgBotV2Service
import com.nikichxp.tgbot.core.service.tgapi.ITgMessageService
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy

class OkxWatchNotificationListenerTest {

    @Test
    fun `notification rejected by telegram is dropped instead of being redelivered`() {
        val listener = OkxWatchNotificationListener(
            stub<ITgMessageService> { throw TgForbiddenException("sendMessage", "Forbidden: bot was blocked by the user") },
            stub<ITgBotV2Service> { TgBotInfo("bot", emptySet()) }
        )

        assertThatCode {
            listener.onNotification(WatchNotification(type = WatchNotificationType.LIST, chatId = 1, botName = "bot"))
        }.doesNotThrowAnyException()
    }

    private inline fun <reified T> stub(crossinline answer: () -> Any): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, _, _ -> answer() } as T
}
