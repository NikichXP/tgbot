package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.logic.ChildActivityRepo
import com.nikichxp.tgbot.childcarebot.logic.ChildInfoRepo
import com.nikichxp.tgbot.childcarebot.logic.ChildStateTransitionProvider
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import com.nikichxp.tgbot.core.util.getContextUserId
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class ChildCareDebugCommandHandler(
    private val tgMessageService: TgMessageService,
    private val childActivityRepo: ChildActivityRepo,
    private val stateTransitionHelper: ChildStateTransitionProvider,
    private val childInfoRepo: ChildInfoRepo,
    private val childCareCommandHandler: ChildCareCommandHandler
) : CommandHandler, Authenticable {

    override fun requiredFeatures(): Set<String> = childCareCommandHandler.requiredFeatures()

    override suspend fun authenticate(update: Update): Boolean = childCareCommandHandler.authenticate(update)

    @HandleCommand("/ctest")
    suspend fun ctest() {
        tgMessageService.sendMessage {
            replyToCurrentMessage()
            text = "ctest"
            withInlineKeyboard(
                listOf(
                    listOf("< 5m" to "minus-5-min"),
                    listOf("5m >" to "plus-5-min")
                )
            )
        }
    }

    @HandleCommand("/debugevents")
    suspend fun getLastEvents(update: Update) {
        val childInfo = update.getContextUserId()?.let { childInfoRepo.findChildByParent(it) }
            ?: throw IllegalStateException("Child not found")

        val lastEvents = childActivityRepo.getLastEvents(childInfo.id, 10)

        val events = lastEvents.joinToString("\n") {
            "${it.date.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))}: ${stateTransitionHelper.getStateText(it.state)}"
        }

        tgMessageService.sendMessage {
            replyToCurrentMessage()
            text = "Last events:\n$events"
        }
    }

}