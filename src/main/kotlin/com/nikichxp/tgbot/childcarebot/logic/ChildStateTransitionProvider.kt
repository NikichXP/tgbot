package com.nikichxp.tgbot.childcarebot.logic

import com.nikichxp.tgbot.childcarebot.ChildActivity
import com.nikichxp.tgbot.childcarebot.ChildStateTransition
import org.springframework.stereotype.Service

@Service
class ChildStateTransitionProvider {

    private val transitions = mutableSetOf<ChildStateTransition>()

    init {
        transition(ChildActivity.SLEEP, ChildActivity.WAKE_UP, "Проснулась")
        transition(ChildActivity.WAKE_UP, ChildActivity.SLEEP, "Уснула")
    }

    fun getStateText(state: ChildActivity): String {
        return when (state) {
            ChildActivity.SLEEP -> "Спит"
            ChildActivity.WAKE_UP -> "Бодрствует"
            ChildActivity.EATING -> "Кушает"
        }
    }

    fun getPossibleTransitions(from: ChildActivity): Map<ChildActivity, String> {
        return transitions
            .filter { it.from == from }
            .associate { it.to to it.name }
    }

    fun getResultState(from: ChildActivity, action: String): ChildActivity? {
        return transitions.find { it.from == from && it.name == action }?.to
    }

    private fun transition(from: ChildActivity, to: ChildActivity, name: String) {
        transitions.add(ChildStateTransition(from, to, name))
    }

}