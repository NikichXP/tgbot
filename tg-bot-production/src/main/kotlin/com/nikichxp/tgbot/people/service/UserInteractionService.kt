package com.nikichxp.tgbot.people.service

import com.nikichxp.tgbot.people.entity.UserInteraction
import com.nikichxp.tgbot.people.repository.UserInteractionRepository
import org.springframework.stereotype.Service

@Service
class UserInteractionService(
    private val userInteractionRepository: UserInteractionRepository
) {

    suspend fun registerUserInteraction(userId: Long, botName: String): Boolean {
        if (userInteractionRepository.exists(userId, botName)) {
            return false
        }
        userInteractionRepository.save(UserInteraction(userId, botName))
        return true
    }
}
