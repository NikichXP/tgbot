package com.nikichxp.tgbot.childcarebot.state

import com.nikichxp.tgbot.childcarebot.*
import com.nikichxp.tgbot.childcarebot.logic.ChildActivityRepo
import com.nikichxp.tgbot.childcarebot.logic.ChildStateTransitionProvider
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class StateTransitionService(
    private val tgOperations: TgOperations,
    private val childActivityRepo: ChildActivityRepo,
    private val stateTransitionHelper: ChildStateTransitionProvider,
    private val transitionHandlers: List<StateTransitionHandler>,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun performStateTransition(
        childInfo: ChildInfo,
        initialState: ChildActivity,
        destinationState: ChildActivity,
        text: String,
    ) {
        val event = childActivityRepo.addActivity(childInfo.id, destinationState)
        val resultKeyboard = stateTransitionHelper.getPossibleTransitions(destinationState).map { it.value }

        for (parentId in childInfo.parents) {
            tgOperations.sendMessage {
                chatId = parentId
                this.text = "State changed to $text"

                withKeyboard(listOf(resultKeyboard))
                withCallback {
                    if (it.ok) {
                        childActivityRepo.addMessageToEvent(
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

        val message = ChildActivityEventMessage(
            event = event,
            transitionDetails = transitionDetails
        )

        applicationEventPublisher.publishEvent(message)

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