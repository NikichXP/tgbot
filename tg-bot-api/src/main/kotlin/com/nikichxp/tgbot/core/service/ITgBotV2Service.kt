package com.nikichxp.tgbot.core.service

import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2Entity

interface ITgBotV2Service {
    fun getAdminBot(): TgBotInfo
    fun getBotById(botId: String): TgBotInfo
    fun getBaseApiFor(botInfo: TgBotInfo): String
    fun listBots(): List<TgBotInfo>
    fun findBotEntity(botId: String): TgBotInfoV2Entity?
    fun saveBotEntity(entity: TgBotInfoV2Entity): TgBotInfoV2Entity
    fun listBotEntities(): List<TgBotInfoV2Entity>
    fun getTokenById(botId: String): String
}
