package com.nikichxp.tgbot.debug.interaction

import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import com.nikichxp.tgbot.debug.SendMessageToAdminService
import io.ktor.util.collections.*
import org.springframework.stereotype.Service

@Service
class NewUserInteractionHandler(
    private val userInteractionService: UserInteractionService,
    private val tgMessageService: TgMessageService,
    private val sendMessageToAdminService: SendMessageToAdminService
) : UpdateHandler {

    private val interactionCache = ConcurrentSet<String>()

    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.ALL)
    override fun requiredFeatures(): Set<String> = setOf()

    override suspend fun handleUpdate(updateContext: UpdateContext) {
        val userId = updateContext.chat?.id ?: return
        val botName = (updateContext.getBotInfo() as TgBotInfo).name
        val key = "$userId:$botName"

        if (interactionCache.contains(key)) {
            return
        }

        val newInteraction = userInteractionService.registerUserInteraction(userId, botName)

        if (newInteraction) {
            val involvedParties = describeInvolvedParties(updateContext)
            sendMessageToAdminService.sendMessage(
                "New user interaction detected: User ID $userId with bot $botName. Involved parties: $involvedParties"
            )
        }

        interactionCache.add(key)
    }

    private fun describeInvolvedParties(updateContext: UpdateContext): String {
        val parts = mutableListOf<String>()
        updateContext.from?.let {
            parts += "from.id:${it.id}"
            it.username?.let { username -> parts += "from.username:$username" }
            parts += "from.fullName:${it.fullName}"
        }
        updateContext.reply?.let { reply ->
            reply.from?.let {
                parts += "replyTo.id:${it.id}"
                it.username?.let { username -> parts += "replyTo.username:$username" }
                parts += "replyTo.fullName:${it.fullName}"
            }
            parts += "replyTo.messageId:${reply.messageId}"
            parts += "replyTo.text:${reply.text ?: ""}"
            parts += "replyTo.chatId:${reply.chat.id}"
            parts += "replyTo.chatType:${reply.chat.type}"
            parts += "replyTo.chatTitle:${reply.chat.title}"
        }
        updateContext.message?.text?.let {
            parts += "message.text:$it"
        }
        return parts.joinToString(" | ")
    }
}

