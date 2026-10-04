package com.nikichxp.tgbot.people.handler

import com.nikichxp.tgbot.core.handlers.BotFeature
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.people.dto.SeenUser
import com.nikichxp.tgbot.people.service.KnownUserService
import org.springframework.stereotype.Component

@Component
class KnownUserTrackingHandler(
    private val knownUserService: KnownUserService
) : UpdateHandler {

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.ALL)
    override fun requiredFeatures(): Set<BotFeature> = emptySet()

    override suspend fun handleUpdate(updateContext: UpdateContext) {
        val botName = (updateContext.getBotInfo() as TgBotInfo).name
        val seenUsers = listOfNotNull(
            updateContext.from?.let { SeenUser(it, botName, updateContext.chat) },
            updateContext.reply?.let { reply -> reply.from?.let { SeenUser(it, botName, reply.chat) } }
        )
        knownUserService.recordSeen(seenUsers)
    }
}
