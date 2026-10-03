package com.nikichxp.tgbot.core.entity

import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import org.springframework.context.ApplicationEvent

class TgUpdateFieldsEvent(
    source: Any,
    val paths: Set<String>,
    val bot: TgBotInfo
) : ApplicationEvent(source)
