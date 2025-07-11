package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.logic.ChildActivityRepo
import com.nikichxp.tgbot.childcarebot.logic.ChildInfoRepo
import com.nikichxp.tgbot.childcarebot.logic.ChildStateTransitionProvider
import com.nikichxp.tgbot.childcarebot.state.StateTransitionService
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.entity.bots.TgBot
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.getContextUserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChildCareCommandHandler(
    private val tgOperations: TgOperations,
    private val childActivityRepo: ChildActivityRepo,
    private val stateTransitionHelper: ChildStateTransitionProvider,
    private val childInfoRepo: ChildInfoRepo,
    private val stateTransitionService: StateTransitionService,
    private val childKeyboardProvider: ChildKeyboardProvider,
) : CommandHandler, UpdateHandler, Authenticable {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun supportedBots(): Set<TgBot> = setOf(TgBot.CHILDTRACKERBOT)

    override suspend fun authenticate(update: Update): Boolean {
        val child = childInfoRepo.findChildByParent(update.getContextUserId()!!)

        if (child == null) {
            logger.warn("No user found for child: user id = ${update.getContextUserId()}")
            return false
        }

        return true
    }

    @HandleCommand("/status")
    suspend fun status(update: Update) {
        val childInfo = update.getContextUserId()?.let { childInfoRepo.findChildByParent(it) }
            ?: throw IllegalStateException("Child not found")
        val lastState = childActivityRepo.getLastEvent(childInfo.id)?.state ?: ChildActivity.WAKE_UP
        val keyboard = childKeyboardProvider.getKeyboardForState(lastState)

        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "Active state: ${stateTransitionHelper.getStateText(lastState)}"
            withKeyboard(keyboard)
        }
    }

    @HandleCommand("/report")
    suspend fun report() {
        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "Выберите отчет"
            withInlineKeyboard(
                listOf(
                    listOf("График сна" to "sleep-schedule")
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

    override fun getMarkers() = setOf(UpdateMarker.MESSAGE_IN_CHAT, UpdateMarker.IS_NOT_COMMAND)

    override suspend fun handleUpdate(update: Update) {
        val text = update.message?.text

        if (text == null) {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                this.text = "No command found"
            }
            return
        } else if (text.startsWith("/")) {
            return
        }

        doStateTransition(text, update.getContextUserId()!!)
    }

    private suspend fun doStateTransition(text: String, userId: Long) {
        val childInfo = childInfoRepo.findChildByParent(userId) ?: throw IllegalStateException("Child not found")
        val currentState = childActivityRepo.getLastEvent(childInfo.id)?.state ?: ChildActivity.WAKE_UP
        val resultState = stateTransitionHelper.getResultState(currentState, text)

        if (resultState != null) {
            stateTransitionService.performStateTransition(childInfo, currentState, resultState, text)
        } else {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                this.text = "Result state is unreachable"
            }
        }
    }

}