package com.nikichxp.tgbot.childcarebot.state

import com.nikichxp.tgbot.childcarebot.ChildActivity
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

class ActiveUpdateSleepTimeJob(
    // info about event to track
) {
    @Id
    @Field(targetType = FieldType.OBJECT_ID)
    lateinit var id: String
}

@Service
class UpdateSleepTimeService() : StateTransitionHandler {

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    fun jobItself() {

    }

    override fun from(): Set<ChildActivity> = ChildActivity.entries.toSet()

    override fun to(): Set<ChildActivity> = ChildActivity.entries.toSet()

    override suspend fun onTransition(transitionDetails: TransitionDetails) {
        TODO("Not yet implemented")
    }


}