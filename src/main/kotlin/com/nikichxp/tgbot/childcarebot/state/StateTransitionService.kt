package com.nikichxp.tgbot.childcarebot.state

import com.nikichxp.tgbot.childcarebot.*
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class StateTransitionService(
    private val tgOperations: TgOperations,
    private val childActivityService: ChildActivityService,
    private val stateTransitionHelper: ChildStateTransitionHelper
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun performStateTransition(
        childInfo: ChildInfo,
        resultState: ChildActivity,
        text: String
    ) {
        val event = childActivityService.addActivity(childInfo.id, resultState)
        val resultKeyboard = stateTransitionHelper.getPossibleTransitions(resultState).map { it.value }

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
    }

}
