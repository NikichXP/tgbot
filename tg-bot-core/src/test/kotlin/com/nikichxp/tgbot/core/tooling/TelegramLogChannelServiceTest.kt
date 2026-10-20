package com.nikichxp.tgbot.core.tooling

import ch.qos.logback.classic.Logger
import com.nikichxp.bot.common.config.AppConfig
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.service.ITgBotV2Service
import com.nikichxp.tgbot.core.service.tgapi.ITgMessageService
import com.nikichxp.tgbot.core.service.tgapi.TgSendMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation

class TelegramLogChannelServiceTest {

    @EnableConfigurationProperties(AppConfig::class)
    class AppConfigOnly

    private val contextRunner = ApplicationContextRunner().withUserConfiguration(AppConfigOnly::class.java)

    @Test
    fun `log channel id is null when the env var is missing or empty`() {
        contextRunner.run { assertThat(it.getBean(AppConfig::class.java).logChannelId).isNull() }
        contextRunner.withPropertyValues("app.logChannelId=")
            .run { assertThat(it.getBean(AppConfig::class.java).logChannelId).isNull() }
        contextRunner.withPropertyValues("app.logChannelId=-1004334163893")
            .run { assertThat(it.getBean(AppConfig::class.java).logChannelId).isEqualTo(-1004334163893) }
    }

    @Test
    fun `without log channel id nothing is attached or sent`() {
        val calls = mutableListOf<String>()
        val service = TelegramLogChannelService(
            recordingStub(calls),
            recordingStub(calls),
            AppConfig(),
            CoroutineScope(Dispatchers.Unconfined)
        )
        val rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger

        service.attach()
        LoggerFactory.getLogger(this::class.java).error("must not reach telegram")
        service.flush()

        assertThat(rootLogger.getAppender("TELEGRAM_CHANNEL")).isNull()
        assertThat(calls).isEmpty()
    }

    @Test
    fun `flush sends formatted log lines to the log channel`() {
        val sent = mutableListOf<TgSendMessage>()
        val service = TelegramLogChannelService(
            capturingMessageService(sent),
            stub<ITgBotV2Service> { TgBotInfo("admin", emptySet()) },
            AppConfig(logChannelId = LOG_CHANNEL_ID),
            CoroutineScope(Dispatchers.Unconfined)
        )

        service.attach()
        LoggerFactory.getLogger("com.example.FooService").warn("hello {}", "channel")
        service.detach()

        assertThat(sent).isNotEmpty.allSatisfy { assertThat(it.chatId).isEqualTo(LOG_CHANNEL_ID) }
        assertThat(sent.joinToString("\n") { it.text })
            .containsPattern("""\d{2}:\d{2}:\d{2}\.\d{3} WARN \[FooService] hello channel""")
    }

    private fun capturingMessageService(sent: MutableList<TgSendMessage>): ITgMessageService =
        stub { args ->
            @Suppress("UNCHECKED_CAST")
            val messageDSL = args!![1] as (TgSendMessage, Continuation<Unit>) -> Any?
            sent += TgSendMessage().also { messageDSL(it, args[2] as Continuation<Unit>) }
            Unit
        }

    private inline fun <reified T> stub(crossinline answer: (Array<out Any?>?) -> Any): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, _, args ->
            answer(args)
        } as T

    private inline fun <reified T> recordingStub(calls: MutableList<String>): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
            calls += method.name
            throw UnsupportedOperationException(method.name)
        } as T

    companion object {
        private const val LOG_CHANNEL_ID = -1004334163893
    }
}
