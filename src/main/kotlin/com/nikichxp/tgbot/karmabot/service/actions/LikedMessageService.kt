package com.nikichxp.tgbot.karmabot.service.actions

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.MessageInteractionResult
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.UserFormatter.getUserPrintName
import com.nikichxp.tgbot.karmabot.service.UserService
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
    private val tgOperations: TgOperations,
    private val likedHistoryService: LikedHistoryService
) {

    suspend fun changeRating(interaction: MessageInteractionResult, update: Update) {
        val messageId = update.message?.messageId ?: throw IllegalStateException()
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
            update = update,
            actor = getUserPrintName(actor),
            target = getUserPrintName(target),
            actorKarma = actorInfo.rating,
            targetKarma = result.get(),
            diff = diff
        )
    }

    private suspend fun sendKarmaMsg(
        update: Update,
        actor: String,
        target: String,
        actorKarma: Double,
        targetKarma: Double,
        diff: Double
    ) {
        val text = "$actor ($actorKarma) changed karma of $target ($targetKarma) Δ=$diff"
        tgOperations.sendMessage(update.message?.chat?.id!!, text)
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