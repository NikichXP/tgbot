package com.nikichxp.tgbot.core.service

import com.nikichxp.tgbot.core.handlers.BotFeature

interface IFeatureRegistry {
    fun allFeatures(): List<BotFeature>
    fun featureIds(): Set<String>
}
