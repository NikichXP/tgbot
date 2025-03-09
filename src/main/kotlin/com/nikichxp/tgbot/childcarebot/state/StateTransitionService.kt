package com.nikichxp.tgbot.childcarebot.state

import com.nikichxp.tgbot.childcarebot.ChildActivity
import com.nikichxp.tgbot.childcarebot.ChildActivityService
import com.nikichxp.tgbot.childcarebot.ChildInfo
import com.nikichxp.tgbot.childcarebot.ChildStateTransitionHelper
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class StateTransitionService(
    private val tgOperations: TgOperations,
    private val childActivityService: ChildActivityService,
    private val stateTransitionHelper: ChildStateTransitionHelper,
    private val transitionHandlers: List<StateTransitionHandler>,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun performStateTransition(
        childInfo: ChildInfo,
        initialState: ChildActivity,
        destinationState: ChildActivity,
        text: String,
    ) {
        val event = childActivityService.addActivity(childInfo.id, destinationState)
        val resultKeyboard = stateTransitionHelper.getPossibleTransitions(destinationState).map { it.value }

        for (parentId in childInfo.parents) {
            tgOperations.sendMessage {
                chatId = parentId
                this.text = "State changed to $text"

                withKeyboard(listOf(resultKeyboard))
                withCallback {
                    if (it.ok) {
                        childActivityService.addMessageToEvent(
                            event.id,
                            chatId = it.result!!.chat.id,
                            messageId = it.result.messageId
                        )
                    }

                    logger.info("Callback: ok = ${it.ok}, message id = ${it.result?.messageId}")
                }
            }
        }

        val transitionDetails = TransitionDetails(
            from = initialState,
            to = destinationState,
            childId = childInfo.id
        )

        transitionHandlers
            .filter { it.from().contains(transitionDetails.from) && it.to().contains(transitionDetails.to) }
            .forEach {
                coroutineScope {
                    launch {
                        it.onTransition(transitionDetails)
                    }
                }
            }
    }

}
