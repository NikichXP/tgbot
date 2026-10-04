package com.nikichxp.tgbot.core.service

import com.nikichxp.tgbot.core.handlers.BotFeature
import com.nikichxp.tgbot.core.handlers.BotSupportFeature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FeatureRegistryImplTest {

    private object Karma : BotFeature("karma", "Karma")
    private object Debug : BotFeature("debug", "Debug")
    private object AnotherKarma : BotFeature("karma", "Another karma")

    private fun requiring(vararg features: BotFeature) = object : BotSupportFeature {
        override fun requiredFeatures() = features.toSet()
    }

    @Test
    fun `collects distinct features from all handlers sorted by id`() {
        val features = FeatureRegistryImpl.collectFeatures(
            listOf(requiring(Karma), requiring(Debug, Karma), requiring())
        )

        assertThat(features).containsExactly(Debug, Karma)
    }

    @Test
    fun `fails when two different features share an id`() {
        assertThrows<IllegalStateException> {
            FeatureRegistryImpl.collectFeatures(listOf(requiring(Karma), requiring(AnotherKarma)))
        }
    }
}
