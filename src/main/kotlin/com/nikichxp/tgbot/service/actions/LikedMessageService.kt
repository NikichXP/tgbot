package com.nikichxp.tgbot.service.actions

import com.nikichxp.tgbot.core.CurrentUpdateProvider
import com.nikichxp.tgbot.entity.MessageInteractionResult
import com.nikichxp.tgbot.service.TgOperations
import com.nikichxp.tgbot.service.UserService
import com.nikichxp.tgbot.util.UserFormatter.getUserPrintName
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
        val messageId = currentUpdateProvider.update?.message?.messageId ?: throw IllegalStateException()
        val actor = interaction.getActor()
        val actorInfo = userService.getUserInfo(actor)
        val target = interaction.getTarget() ?: throw IllegalStateException(impossibleStateOfNoTarget)
        val diff = calculateKarmaDiff(actorInfo.rating, interaction.power)
        val result = AtomicReference(0.0)
        userService.modifyUser(target) {
            it.rating = roundF(it.rating + diff)
            result.set(it.rating)
            runBlocking {
                launch {
                    likedHistoryService.report(actorInfo.id, target.id, messageId, interaction.power)
                }
            }
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

    companion object {
        // TODO this should be a part of some properties file with errors
        private const val powerMultiplier = 0.4
        private const val impossibleStateOfNoTarget =
            "[INT0001: no interaction target found when it is supposed to be a target]"

        fun calculateKarmaDiff(actorRating: Double, power: Double): Double {
            if (actorRating < 0.0) {
                return 0.0
            }
            val calculatedDiff = actorRating.pow(powerMultiplier)
            return roundF(calculatedDiff).coerceAtLeast(1.0) * power
        }

        /**
         * F suffix in name to not mismatch with Math.round(..) or anything like that
         */
        fun roundF(value: Double) = BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).toDouble()
    }

}