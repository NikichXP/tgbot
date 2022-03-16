package com.nikichxp.tgbot.service.actions

import com.nikichxp.tgbot.core.CurrentUpdateProvider
import com.nikichxp.tgbot.dto.User
import com.nikichxp.tgbot.entity.MessageInteractionResult
import com.nikichxp.tgbot.service.TgOperations
import com.nikichxp.tgbot.service.UserInfo
import com.nikichxp.tgbot.service.UserService
import com.nikichxp.tgbot.util.UserFormatter.getUserPrintName
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.pow

@Service
class LikedMessageService(
    private val userService: UserService,
    private val currentUpdateProvider: CurrentUpdateProvider,
    private val tgOperations: TgOperations,
    private val likedHistoryService: LikedHistoryService
) {

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
        sendKarmaMsg(
            actor = getUserPrintName(actor),
            target = getUserPrintName(target),
            actorKarma = actorInfo.rating,
            targetKarma = result.get(),
            diff = diff
        )
    }

    private fun sendKarmaMsg(actor: String, target: String, actorKarma: Double, targetKarma: Double, diff: Double) {
        val text = "$actor ($actorKarma) changed karma of $target ($targetKarma) Δ=$diff"
        tgOperations.sendMessage(currentUpdateProvider.update?.message?.chat?.id?.toString()!!, text)
    }

    private fun calculateKarmaDiff(actor: UserInfo, target: User, interaction: MessageInteractionResult): Double {
        val messageId = currentUpdateProvider.update?.message?.messageId ?: throw IllegalStateException()
        likedHistoryService.report(actor.id, target.id, messageId)
        val calculatedDiff = (1 + actor.rating.pow(powerMultiplier)) * interaction.power
        return BigDecimal.valueOf(calculatedDiff).setScale(3, RoundingMode.HALF_UP).toDouble()
    }

    // TODO this should be a part of some properties file with errors
    companion object {
        private const val powerMultiplier = 0.4
        private const val impossibleStateOfNoTarget =
            "[INT0001: no interaction target found when it is supposed to be a target]"
    }

}