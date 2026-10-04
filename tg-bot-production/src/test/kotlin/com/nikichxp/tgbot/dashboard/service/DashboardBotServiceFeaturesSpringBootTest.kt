package com.nikichxp.tgbot.dashboard.service

import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2Entity
import com.nikichxp.tgbot.core.handlers.BotFeature
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.service.FeatureRegistryImpl
import com.nikichxp.tgbot.core.service.ITgBotV2Service
import com.nikichxp.tgbot.core.service.tgapi.TgRegisterUpdateFetchService
import com.nikichxp.tgbot.dashboard.connector.TelegramBotApiClient
import com.nikichxp.tgbot.dashboard.converters.BotFeatureToDashboardFeatureDtoConverter
import com.nikichxp.tgbot.dashboard.dto.DashboardFeatureDto
import com.nikichxp.tgbot.dashboard.dto.UpdateBotFeaturesRequest
import com.nikichxp.tgbot.debug.DebugFeature
import com.nikichxp.tgbot.karmabot.KarmaFeature
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.convert.ApplicationConversionService
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.ConversionService
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(
    classes = [
        DashboardBotService::class,
        FeatureRegistryImpl::class,
        BotFeatureToDashboardFeatureDtoConverter::class,
        DashboardBotServiceFeaturesSpringBootTest.Config::class
    ]
)
class DashboardBotServiceFeaturesSpringBootTest {

    @MockitoBean
    lateinit var tgBotV2Service: ITgBotV2Service

    @MockitoBean
    lateinit var tgRegisterUpdateFetchService: TgRegisterUpdateFetchService

    @MockitoBean
    lateinit var telegramBotApiClient: TelegramBotApiClient

    @Autowired
    lateinit var dashboardBotService: DashboardBotService

    private val bot = TgBotInfoV2Entity(BOT_NAME).apply {
        token = "token"
        supportedFeatures = setOf(KarmaFeature.id, LEGACY_FEATURE_ID)
    }

    @BeforeEach
    fun stubBotStorage() {
        `when`(tgBotV2Service.findBotEntity(BOT_NAME)).thenReturn(bot)
        `when`(tgBotV2Service.saveBotEntity(anyBotEntity())).thenAnswer { it.getArgument<TgBotInfoV2Entity>(0) }
    }

    private fun anyBotEntity(): TgBotInfoV2Entity = any(TgBotInfoV2Entity::class.java) ?: bot

    @Test
    fun `available features come from the handlers deployed in the application`() {
        assertThat(dashboardBotService.availableFeatures()).containsExactly(
            DashboardFeatureDto(DebugFeature.id, DebugFeature.title, DebugFeature.description),
            DashboardFeatureDto(KarmaFeature.id, KarmaFeature.title, KarmaFeature.description)
        )
    }

    @Test
    fun `bot features can be set to registered features`() {
        val saved = runBlocking {
            dashboardBotService.updateFeatures(BOT_NAME, UpdateBotFeaturesRequest(setOf(DebugFeature.id)))
        }

        assertThat(saved.supportedFeatures).containsExactly(DebugFeature.id)
    }

    @Test
    fun `feature already set on the bot stays allowed even if no module declares it`() {
        val saved = runBlocking {
            dashboardBotService.updateFeatures(BOT_NAME, UpdateBotFeaturesRequest(setOf(LEGACY_FEATURE_ID)))
        }

        assertThat(saved.supportedFeatures).containsExactly(LEGACY_FEATURE_ID)
    }

    @Test
    fun `unknown feature is rejected and nothing is saved`() {
        assertThatThrownBy {
            runBlocking {
                dashboardBotService.updateFeatures(BOT_NAME, UpdateBotFeaturesRequest(setOf("teleport")))
            }
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Unknown features: teleport")

        verify(tgBotV2Service, never()).saveBotEntity(anyBotEntity())
    }

    @TestConfiguration
    class Config {

        @Bean
        fun conversionService(converter: BotFeatureToDashboardFeatureDtoConverter): ConversionService =
            ApplicationConversionService().apply { addConverter(converter) }

        @Bean
        fun debugCommandHandler(): CommandHandler = commandHandler(DebugFeature)

        @Bean
        fun karmaCommandHandler(): CommandHandler = commandHandler(KarmaFeature)

        private fun commandHandler(vararg features: BotFeature) = object : CommandHandler {
            override fun requiredFeatures() = features.toSet()
        }
    }

    companion object {
        const val BOT_NAME = "test-bot"
        const val LEGACY_FEATURE_ID = "shitposting"
    }
}
