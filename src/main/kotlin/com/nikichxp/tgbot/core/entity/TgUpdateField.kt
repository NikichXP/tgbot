package com.nikichxp.tgbot.core.entity

import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("tgUpdateField")
data class TgUpdateField(
    @Indexed(unique = true)
    var path: String,
    var bot: TgBotInfo,
    var created: LocalDateTime = LocalDateTime.now()
)
