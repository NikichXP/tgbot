package com.nikichxp.tgbot.childcarebot.state

import com.nikichxp.tgbot.childcarebot.ChildActivity
import com.nikichxp.tgbot.childcarebot.ChildActivityService
import com.nikichxp.tgbot.childcarebot.TgMessageInfo
import jakarta.annotation.PostConstruct
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class UpdateSleepTimeService(
    private val childActivityService: ChildActivityService
) : StateTransitionHandler {

    private val trackingEntities = mutableMapOf<Long, TgMessageInfo>()

    @PostConstruct
    fun findLastEvents() {
        val addKids = childActivityService.getAllChildrenThatHasEvents()
        addKids.forEach{ kid ->
            val action = childActivityService.getLatestState(kid)
            if (action == ChildActivity.SLEEP) {

            }
        }
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    fun jobItself() {

    }

    override fun from(): Set<ChildActivity> = ChildActivity.entries.toSet()

    override fun to(): Set<ChildActivity> = ChildActivity.entries.toSet()

    override suspend fun onTransition(transitionDetails: TransitionDetails) {
//        TODO("Not yet implemented")
    }


}