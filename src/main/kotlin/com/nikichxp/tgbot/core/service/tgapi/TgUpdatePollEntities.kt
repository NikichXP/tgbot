package com.nikichxp.tgbot.core.service.tgapi

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.bots.TgBotInfoV2
import java.time.LocalDateTime
import java.util.LinkedList


data class TgResponse(
    val ok: Boolean,
    val result: List<Update>
)

data class PollingInfo(
    val bot: TgBotInfoV2,
    var lastUpdate: Long,
    var lastUpdateFetched: LocalDateTime
) {

    lateinit var token: String

    var lastUpdateExpiryDate = lastUpdateFetched.plusDays(1).plusHours(1)

    private val processedUpdates = LinkedList<Long>()

    fun shouldBeProcessed(updateId: Long): Boolean {
        return !processedUpdates.contains(updateId)
    }

    fun onProcess(updateId: Long): Boolean {
        processedUpdates.add(updateId)
        while (processedUpdates.size > 1_000) {
            processedUpdates.poll()
        }
        if (updateId > lastUpdate) {
            lastUpdate = updateId
            return true
        }
        return false
    }

    override fun toString(): String {
        return "PollingInfo(${bot.name}, update $lastUpdate, lastUpdateFetched: $lastUpdateFetched)"
    }
}
