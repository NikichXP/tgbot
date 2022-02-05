package com.nikichxp.tgbot.service.actions

import com.nikichxp.tgbot.core.CurrentUpdateProvider
import com.nikichxp.tgbot.dto.User
import com.nikichxp.tgbot.entity.MessageInteractionResult
import com.nikichxp.tgbot.service.TgOperations
import com.nikichxp.tgbot.service.UserInfo
import com.nikichxp.tgbot.service.UserService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicReference

@Service
class LikedMessageService(
    private val mongoTemplate: MongoTemplate,
    private val userService: UserService,
    private val currentUpdateProvider: CurrentUpdateProvider,
    private val tgOperations: TgOperations
) {

    // TODO save history of karma givers
    fun changeRating(interaction: MessageInteractionResult) {
        val actor = interaction.getActor()
        val actorInfo = userService.getUserInfo(actor)
        val target = interaction.getTarget() ?: throw IllegalStateException(impossibleStateOfNoTarget)
        val diff = calculateKarmaDiff(actorInfo, target, interaction)
        val result = AtomicReference(0.0)
        userService.modifyUser(target) {
            it.rating += diff
            result.set(it.rating)
        }
        val text = "${getUserPrintName(actor)} changed karma of ${getUserPrintName(target)} (${result.get()})"
        tgOperations.sendMessage(currentUpdateProvider.update?.message?.chat?.id?.toString()!!, text)
    }

    private fun calculateKarmaDiff(actor: UserInfo, target: User, interaction: MessageInteractionResult): Double {
        return 1 + (actor.rating / 10)
    }

    fun getUserPrintName(user: User): String {
        return when {
            user.username != null -> user.username
            user.lastName != null -> "${user.firstName} ${user.lastName}"
            else -> user.firstName
        }
    }

    // TODO this should be a part of some properties file with errors
    companion object {
        const val impossibleStateOfNoTarget =
            "[INT0001: no interaction target found when it is supposed to be a target]"
    }

}