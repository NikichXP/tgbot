package com.nikichxp.tgbot.core.tooling

import ch.qos.logback.classic.Logger
import com.nikichxp.bot.common.config.AppConfig
import com.nikichxp.tgbot.core.service.ITgBotV2Service
import com.nikichxp.tgbot.core.service.tgapi.ITgMessageService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.lang.reflect.Proxy

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
            AppConfig()
        )
        val rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger

        service.attach()
        LoggerFactory.getLogger(this::class.java).error("must not reach telegram")
        service.flush()

        assertThat(rootLogger.getAppender("TELEGRAM_CHANNEL")).isNull()
        assertThat(calls).isEmpty()
    }

    private inline fun <reified T> recordingStub(calls: MutableList<String>): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
            calls += method.name
            throw UnsupportedOperationException(method.name)
        } as T
}
