package com.nikichxp.tgbot.childcarebot

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class ChildStateTransitionService {

    private val transitions = mutableSetOf<ChildStateTransition>()

    @PostConstruct
    fun init() {
        transition(ChildActivity.SLEEP, ChildActivity.WAKE_UP, "Проснулась")
        transition(ChildActivity.WAKE_UP, ChildActivity.SLEEP, "Уснула")
        transition(ChildActivity.WAKE_UP, ChildActivity.EATING, "Кушает")
        transition(ChildActivity.EATING, ChildActivity.WAKE_UP, "Доела")
    }

    private fun transition(from: ChildActivity, to: ChildActivity, name: String) {
        transitions.add(ChildStateTransition(from, to, name))
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

}