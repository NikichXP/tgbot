package com.nikichxp.tgbot.childcarebot.state

import com.nikichxp.tgbot.childcarebot.ChildActivity
import org.springframework.stereotype.Indexed

@Indexed
interface StateTransitionHandler {

    fun from(): Set<ChildActivity>
    fun to(): Set<ChildActivity>

    suspend fun onTransition(transitionDetails: TransitionDetails)

}

data class TransitionDetails(
    val from: ChildActivity,
    val to: ChildActivity,
    val childId: Long
)
