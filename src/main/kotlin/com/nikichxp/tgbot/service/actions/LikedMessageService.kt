package com.nikichxp.tgbot.service.actions

import com.nikichxp.tgbot.core.CurrentUpdateProvider
import com.nikichxp.tgbot.entity.MessageInteractionResult
import com.nikichxp.tgbot.service.TgOperations
import com.nikichxp.tgbot.service.UserService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service

@Service
class LikedMessageService(
    private val mongoTemplate: MongoTemplate,
    private val userService: UserService,
    private val currentUpdateProvider: CurrentUpdateProvider,
    private val tgOperations: TgOperations
) {

    // TODO save history of karma givers
    fun changeRating(interaction: MessageInteractionResult) {
        val target = interaction.getTarget() ?: throw IllegalStateException(impossibleStateOfNoTarget)
        userService.modifyUser(target) {
            it.rating += interaction.power
        }
        val text = "${target.id} karma is set with participants ${interaction.users.toString()}"
        tgOperations.sendMessage(currentUpdateProvider.update?.message?.chat?.id?.toString()!!, text)
     }

    // TODO this should be a part of some properties file with errors
    companion object {
        const val impossibleStateOfNoTarget = "[INT0001: no interaction target found when it is supposed to be a target]"
    }

}