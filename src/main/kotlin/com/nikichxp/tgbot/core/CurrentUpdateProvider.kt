package com.nikichxp.tgbot.core

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@RequestScope
@Component
class CurrentUpdateProvider {

    var update: Update? = null
    lateinit var bot: TgBot

}