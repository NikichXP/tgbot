package com.nikichxp.tgbot.childcarebot.state

import com.nikichxp.tgbot.childcarebot.ChildActivity
import org.springframework.stereotype.Component
import org.springframework.stereotype.Indexed
import org.springframework.stereotype.Service

@Indexed
interface StateTransitionHandler {

    fun from(state: ChildActivity): Boolean
    fun to(state: ChildActivity): Boolean

    fun doTransition(from: ChildActivity, to: ChildActivity)

}

@Service
class StateTransitionHandlerFactory {

    // ???

}

class AddTimeNavigationHandler : StateTransitionHandler {
    override fun from(state: ChildActivity): Boolean {
        TODO("Not yet implemented")
    }

    override fun to(state: ChildActivity): Boolean {
        TODO("Not yet implemented")
    }

    override fun doTransition(from: ChildActivity, to: ChildActivity) {
        TODO("Not yet implemented")
    }

}