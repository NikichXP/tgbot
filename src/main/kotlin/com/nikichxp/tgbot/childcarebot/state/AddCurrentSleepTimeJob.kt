package com.nikichxp.tgbot.childcarebot.state

import com.nikichxp.tgbot.childcarebot.*
import com.nikichxp.tgbot.childcarebot.logic.ChildActivityRepo
import com.nikichxp.tgbot.childcarebot.logic.ChildStateTransitionProvider
import com.nikichxp.tgbot.core.entity.bots.TgBot
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UpdateSleepTimeService(
    private val childActivityRepo: ChildActivityRepo,
    private val childStateTransitionProvider: ChildStateTransitionProvider,
    private val tgOperations: TgOperations,
) : ApplicationListener<ChildActivityEventMessage> {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val trackingEntities = mutableMapOf<Long, ChildActivityEvent>()

    @PostConstruct
    fun findLastEvents() {
        val addKids = childActivityRepo.getAllChildrenThatHasEvents()

        addKids.forEach { loadChild(it) }
    }

//    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    fun jobItself() {
        runBlocking {
            trackingEntities.forEach { (_, eventState) ->
                val statusMessage = StringBuilder()
                    .append(childStateTransitionProvider.getStateText(eventState.activity))
                    .append(" (")
                    .append(getDurationStringBetween(eventState.date, LocalDateTime.now()))
                    .append(')')
                    .toString()
                eventState.sentMessages.forEach { message ->
                    tgOperations.updateMessageText(
                        chatId = message.chatId,
                        messageId = message.messageId,
                        text = statusMessage,
                        bot = TgBot.CHILDTRACKERBOT
                    )
                }
            }
        }
    }

    override fun onApplicationEvent(event: ChildActivityEventMessage) {
        logger.info("Update sleep time event listener for id: ${event.event.childId}")
        loadChild(event.event.childId)
    }

    private fun loadChild(childId: Long) {
        val event = childActivityRepo.getLastEvent(childId) ?: return
        trackingEntities[event.childId] = event
    }

}