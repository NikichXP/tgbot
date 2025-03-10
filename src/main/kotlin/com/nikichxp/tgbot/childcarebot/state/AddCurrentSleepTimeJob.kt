package com.nikichxp.tgbot.childcarebot.state

import com.nikichxp.tgbot.childcarebot.ChildActivity
import com.nikichxp.tgbot.childcarebot.ChildActivityEvent
import com.nikichxp.tgbot.childcarebot.ChildActivityEventMessage
import com.nikichxp.tgbot.childcarebot.ChildActivityService
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class UpdateSleepTimeService(
    private val childActivityService: ChildActivityService
) : StateTransitionHandler, ApplicationListener<ChildActivityEventMessage> {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val trackingEntities = mutableMapOf<Long, ChildActivityEvent>()

    @PostConstruct
    fun findLastEvents() {
        val addKids = childActivityService.getAllChildrenThatHasEvents()

        addKids.mapNotNull { childActivityService.getLastActivity(it) }
            .filter { it.activity == ChildActivity.SLEEP }
            .forEach { trackingEntities[it.childId] = it }
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    fun jobItself() {

    }

    override fun from(): Set<ChildActivity> = ChildActivity.entries.toSet()

    override fun to(): Set<ChildActivity> = ChildActivity.entries.toSet()

    override suspend fun onTransition(transitionDetails: TransitionDetails) {
//        TODO("Not yet implemented")
    }

    override fun onApplicationEvent(event: ChildActivityEventMessage) {
        logger.info("Update sleep time event listener")
    }


}