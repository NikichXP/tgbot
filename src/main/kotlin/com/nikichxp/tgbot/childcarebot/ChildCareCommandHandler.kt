package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.getContextChatId
import org.springframework.stereotype.Service

@Service
class ChildCareCommandHandler(
    private val tgOperations: TgOperations,
    private val childActivityService: ChildActivityService,
    private val appConfig: AppConfig,
    private val childStateTransitionFactory: ChildStateTransitionFactory
) : CommandHandler, UpdateHandler, Authenticable {

    private val buttonStateMap = mapOf(
        ChildActivity.SLEEP to "Уснула",
        ChildActivity.WAKE_UP to "Проснулась",
        ChildActivity.EATING to "Кушает"
    )

    private val stateButtonMap = buttonStateMap.map { it.value to it.key }.toMap()

    private val possibleTransitions = mapOf(
        ChildActivity.SLEEP to setOf(ChildActivity.WAKE_UP),
        ChildActivity.WAKE_UP to setOf(ChildActivity.EATING, ChildActivity.SLEEP),
        ChildActivity.EATING to setOf(ChildActivity.SLEEP)
    )

    override fun supportedBots(): Set<TgBot> = setOf(TgBot.CHILDTRACKERBOT)

    override suspend fun authenticate(update: Update): Boolean {
        if (update.getContextChatId() != appConfig.adminId) {
            tgOperations.replyToCurrentMessage("You are not allowed to use this bot ~_~")
            return false
        }
        return true
    }

    @HandleCommand("/status")
    suspend fun status() {
        val lastState = childActivityService.getLatestState()
        val buttons = getButtonsForState(lastState)

        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "Active state: ${buttonStateMap[lastState]}"
            withKeyboard(listOf(buttons))
        }
    }

    @HandleCommand("/report")
    suspend fun report() {
        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = childActivityService.getActivities().joinToString("\n") { "${it.activity} at ${it.date}" }
        }
    }

    private fun getButtonsForState(state: ChildActivity): List<String> {
        return childStateTransitionFactory.getPossibleTransitions(state).map { it.value }
    }

    override fun botSupported(bot: TgBot): Boolean = bot == TgBot.CHILDTRACKERBOT

    override fun getMarkers() = setOf(UpdateMarker.MESSAGE_IN_CHAT, UpdateMarker.IS_NOT_COMMAND)

    override suspend fun handleUpdate(update: Update) {
        val command = update.message?.text

        if (command == null) {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = "No command found"
            }
            return
        }

        val currentState = childActivityService.getLatestState()
        val possibleCommandStates = possibleTransitions[currentState]?.map(buttonStateMap::get) ?: emptyList()

        if (command in possibleCommandStates) {
            val newState = stateButtonMap[command] ?: return
            childActivityService.addActivity(newState)
            tgOperations.sendMessage {
                text = "State changed to $command"
                replyToCurrentMessage()
                withKeyboard(listOf(getButtonsForState(newState)))
            }
        } else {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = "not yet implemented"
            }
        }
    }
}