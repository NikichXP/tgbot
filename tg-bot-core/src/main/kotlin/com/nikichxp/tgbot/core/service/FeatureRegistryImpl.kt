package com.nikichxp.tgbot.core.service

import com.nikichxp.bot.common.handlers.BotFeature
import com.nikichxp.bot.common.handlers.BotSupportFeature
import com.nikichxp.bot.common.service.IFeatureRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.stereotype.Service

@Service
class FeatureRegistryImpl(
    private val featureUsers: ObjectProvider<BotSupportFeature>
) : IFeatureRegistry, SmartInitializingSingleton {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var features: List<BotFeature>

    override fun afterSingletonsInstantiated() {
        features = collectFeatures(featureUsers.orderedStream().toList())
        logger.info("Registered bot features: {}", features.joinToString { it.id })
    }

    override fun allFeatures(): List<BotFeature> = features

    override fun featureIds(): Set<String> = features.mapTo(mutableSetOf()) { it.id }

    companion object {
        fun collectFeatures(featureUsers: List<BotSupportFeature>): List<BotFeature> {
            val declared = featureUsers.flatMap { it.requiredFeatures() }.distinct()
            val conflictingIds = declared.groupBy { it.id }.filterValues { it.size > 1 }.keys
            check(conflictingIds.isEmpty()) { "Bot features declared more than once with the same id: $conflictingIds" }
            return declared.sortedBy { it.id }
        }
    }
}
