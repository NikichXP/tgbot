package com.nikichxp.tgbot.dashboard.dto

import com.nikichxp.tgbot.core.entity.bots.TgUpdateFetchType

data class DashboardBotDto(
    val name: String,
    val updateFetchType: TgUpdateFetchType,
    val supportedFeatures: Set<String>
)

data class CreateBotRequest(
    val name: String? = null,
    val token: String,
    val updateFetchType: TgUpdateFetchType = TgUpdateFetchType.WEBHOOK,
    val supportedFeatures: Set<String> = emptySet()
)

data class UpdateBotFeaturesRequest(val supportedFeatures: Set<String>)
