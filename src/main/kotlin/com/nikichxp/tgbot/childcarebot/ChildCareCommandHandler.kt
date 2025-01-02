package com.nikichxp.tgbot.childcarebot

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackContext
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.getContextUserId
import org.springframework.stereotype.Service

@Service
class ChildCareCommandHandler(
    private val tgOperations: TgOperations,
    private val childActivityService: ChildActivityService,
    private val appConfig: AppConfig,
    private val stateTransitionService: ChildStateTransitionService,
    private val childInfoService: ChildInfoService,
    private val objectMapper: ObjectMapper
) : CommandHandler, UpdateHandler, CallbackHandler, Authenticable {


    override fun supportedBots(): Set<TgBot> = setOf(TgBot.CHILDTRACKERBOT)

    override suspend fun authenticate(update: Update): Boolean {
        val child = childInfoService.findChildByParent(update.getContextUserId()!!)

        if (child == null) {
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
            text = "Active state: ${stateTransitionService.getStateText(lastState)}"
            withKeyboard(listOf(buttons))
        }
    }

    @HandleCommand("/report")
    suspend fun report() {
        tgOperations.sendMessage {
            replyToCurrentMessage()
//            text = childActivityService.getActivities()
//                .map { stateTransitionService.getStateText(it.activity) to it.date }
//                .joinToString("\n") { "${it.first} at ${it.second}" }
            text = "Выберите отчет"
            withInlineKeyboard(
                listOf(
                    listOf("График сна" to "sleep-schedule"),
                    listOf("График кормления" to "feeding-schedule")
                )
            )
        }
    }

    @HandleCommand("/ctest")
    suspend fun ctest() {
        tgOperations.sendMessage {
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

    private fun getButtonsForState(state: ChildActivity): List<String> {
        return stateTransitionService.getPossibleTransitions(state).map { it.value }
    }

    override fun getMarkers() = setOf(UpdateMarker.MESSAGE_IN_CHAT, UpdateMarker.IS_NOT_COMMAND)

    override suspend fun handleUpdate(update: Update) {
        val command = update.message?.text

        if (command == null) {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = "No command found"
            }
            return
        } else if (command.startsWith("/")) {
            return
        }

        val currentState = childActivityService.getLatestState()
        val resultState = stateTransitionService.getResultState(currentState, command)

        if (resultState != null) {
            childActivityService.addActivity(resultState)
            tgOperations.sendMessage {
                text = "State changed to $command"
                replyToCurrentMessage()
                withKeyboard(listOf(getButtonsForState(resultState)))
            }
        } else {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = "not yet implemented"
            }
        }
    }


    override fun isCallbackSupported(callbackContext: CallbackContext): Boolean =
        callbackContext.bot == TgBot.CHILDTRACKERBOT

    override suspend fun handleCallback(
        callbackContext: CallbackContext,
        update: Update
    ): Boolean {
        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "Got callback: ${objectMapper.writeValueAsString(update)}"
        }
    }
}