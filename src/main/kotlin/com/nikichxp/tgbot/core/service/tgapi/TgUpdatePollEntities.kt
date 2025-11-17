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

    private val processedUpdates = LinkedList<Long>()

    init {
        processedUpdates.add(lastUpdate)
    }

    lateinit var token: String

    var lastUpdateExpiryDate = updateExpiryDate(lastUpdateFetched)

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
            lastUpdateExpiryDate = updateExpiryDate(LocalDateTime.now())
            return true
        }
        return false
    }

    private fun updateExpiryDate(since: LocalDateTime): LocalDateTime? {
        return since.plusDays(1).plusHours(1)
    }

    override fun toString(): String {
        return "PollingInfo(${bot.name}, update $lastUpdate, lastUpdateFetched: $lastUpdateFetched)"
    }
}
