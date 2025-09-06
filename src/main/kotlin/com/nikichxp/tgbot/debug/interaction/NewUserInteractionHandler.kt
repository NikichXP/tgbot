package com.nikichxp.tgbot.debug.interaction

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.core.util.getContextInvolvedParties
import com.nikichxp.tgbot.debug.SendMessageToAdminService
import io.ktor.util.collections.ConcurrentSet
import org.springframework.stereotype.Service

@Service
class NewUserInteractionHandler(
    private val userInteractionService: UserInteractionService,
    private val tgOperations: TgOperations,
    private val sendMessageToAdminService: SendMessageToAdminService
) : UpdateHandler {

    private val interactionCache = ConcurrentSet<String>()

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.ALL)
    override fun requiredFeatures(): Set<String> = setOf()

    override suspend fun handleUpdate(update: Update) {
        val userId = update.getContextChatId() ?: return
        val botName = update.bot.name
        val key = "$userId:$botName"

        if (interactionCache.contains(key)) {
            return
        }

        val interactionHappenedBefore = userInteractionService.registerUserInteraction(userId, botName)

        if (!interactionHappenedBefore) {
            val involvedParties = update.getContextInvolvedParties().asSequence()
                .map { pair -> "${pair.key}:${pair.value}" }
                .reduce { a, b -> "$a | $b" }
            sendMessageToAdminService.sendMessage(
                "New user interaction detected: User ID $userId with bot $botName. Involved parties: $involvedParties"
            )
        }

        interactionCache.add(key)
    }
}

