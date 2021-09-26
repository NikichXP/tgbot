package com.nikichxp.tgbot.service.actions

import com.nikichxp.tgbot.dto.User
import com.nikichxp.tgbot.entity.MessageInteractionResult
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service

@Service
class LikedMessageService(
    private val mongoTemplate: MongoTemplate
) {

    fun changeRating(interaction: MessageInteractionResult) {

    }

    fun changeRating(userToChange: User, author: User, force: Double) {

    }



}