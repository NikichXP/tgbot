package com.nikichxp.bot.common.service

import com.nikichxp.bot.common.handlers.BotFeature

interface IFeatureRegistry {
    fun allFeatures(): List<BotFeature>
    fun featureIds(): Set<String>
}
