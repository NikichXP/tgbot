package com.nikichxp.tgbot.core.service

import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.BotFeature
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackContext
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean

@SpringBootTest(classes = [FeatureRegistryImpl::class, FeatureRegistrySpringBootTest.Handlers::class])
class FeatureRegistrySpringBootTest {

    object Karma : BotFeature("karma", "Karma", "Reputation in group chats")
    object Summary : BotFeature("summary", "Summary")
    object Debug : BotFeature("debug", "Debug")
    object ConflictingKarma : BotFeature("karma", "Conflicting karma")

    @Autowired
    lateinit var featureRegistry: IFeatureRegistry

    @Test
    fun `registry contains every feature required by update, command and callback handlers`() {
        assertThat(featureRegistry.allFeatures()).containsExactly(Debug, Karma, Summary)
        assertThat(featureRegistry.featureIds()).containsExactlyInAnyOrder("debug", "karma", "summary")
    }

    @Test
    fun `feature metadata is exposed as declared by the module`() {
        val karma = featureRegistry.allFeatures().single { it.id == "karma" }

        assertThat(karma.title).isEqualTo("Karma")
        assertThat(karma.description).isEqualTo("Reputation in group chats")
    }

    @Test
    fun `context fails to start when two different features share an id`() {
        ApplicationContextRunner()
            .withUserConfiguration(FeatureRegistryImpl::class.java, ConflictingHandlers::class.java)
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(context.startupFailure)
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessage(
                    "Bot features declared more than once with the same id: [karma]"
                )
            }
    }

    @Test
    fun `context starts with an empty registry when no handler requires a feature`() {
        ApplicationContextRunner()
            .withUserConfiguration(FeatureRegistryImpl::class.java)
            .withBean("featurelessHandler", CommandHandler::class.java, { commandHandler() })
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context.getBean(IFeatureRegistry::class.java).allFeatures()).isEmpty()
            }
    }

    @TestConfiguration
    class Handlers {

        @Bean
        fun karmaUpdateHandler(): UpdateHandler = updateHandler(Karma)

        @Bean
        fun karmaAndSummaryCommandHandler(): CommandHandler = commandHandler(Karma, Summary)

        @Bean
        fun debugCallbackHandler(): CallbackHandler = callbackHandler(Debug)

        @Bean
        fun featurelessCommandHandler(): CommandHandler = commandHandler()
    }

    @TestConfiguration
    class ConflictingHandlers {

        @Bean
        fun karmaCommandHandler(): CommandHandler = commandHandler(Karma)

        @Bean
        fun conflictingKarmaCommandHandler(): CommandHandler = commandHandler(ConflictingKarma)
    }

    companion object {

        fun commandHandler(vararg features: BotFeature) = object : CommandHandler {
            override fun requiredFeatures() = features.toSet()
        }

        fun updateHandler(vararg features: BotFeature) = object : UpdateHandler {
            override fun requiredFeatures() = features.toSet()
            override fun getMarkers() = setOf(UpdateMarker.ALL)
            override suspend fun handleUpdate(updateContext: UpdateContext) = Unit
        }

        fun callbackHandler(vararg features: BotFeature) = object : CallbackHandler {
            override fun requiredFeatures() = features.toSet()
            override fun isCallbackSupported(callbackContext: CallbackContext) = false
            override suspend fun handleCallback(callbackContext: CallbackContext) = false
        }
    }
}
