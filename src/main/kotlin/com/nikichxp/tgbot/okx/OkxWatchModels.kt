package com.nikichxp.tgbot.okx

const val OKX_WATCH_COMMANDS_QUEUE = "okx.watch.commands"
const val OKX_WATCH_NOTIFICATIONS_QUEUE = "okx.watch.notifications"

enum class ThresholdType { PERCENT, ABSOLUTE }

enum class WatchAction { WATCH, UNWATCH, LIST }

enum class MoveDirection { UP, DOWN }

enum class WatchNotificationType { REGISTERED, UNREGISTERED, TRIGGERED, LIST, ERROR }

data class Threshold(val type: ThresholdType, val value: Double) {

    override fun toString() = when (type) {
        ThresholdType.PERCENT -> "${formatNumber(value)}%"
        ThresholdType.ABSOLUTE -> formatNumber(value)
    }

    companion object {
        /** `2%`, `2.5%`, `2,5%` -> percent; `500`, `850.5` -> absolute price change. */
        fun parse(raw: String): Threshold? {
            val s = raw.trim().replace(',', '.')
            val type = if (s.endsWith("%")) ThresholdType.PERCENT else ThresholdType.ABSOLUTE
            val value = s.removeSuffix("%").toDoubleOrNull() ?: return null
            if (!value.isFinite() || value <= 0) return null
            return Threshold(type, value)
        }
    }
}

data class WatchCommand(
    val action: WatchAction,
    val chatId: Long,
    val botName: String,
    val instrumentId: String? = null,
    val thresholdType: ThresholdType? = null,
    val thresholdValue: Double? = null,
)

data class WatchInfo(
    val instrumentId: String,
    val thresholdType: ThresholdType,
    val thresholdValue: Double,
    val price: Double? = null,
)

data class WatchNotification(
    val type: WatchNotificationType,
    val chatId: Long,
    val botName: String,
    val instrumentId: String? = null,
    val thresholdType: ThresholdType? = null,
    val thresholdValue: Double? = null,
    val direction: MoveDirection? = null,
    val fromPrice: Double? = null,
    val price: Double? = null,
    val changePercent: Double? = null,
    val message: String? = null,
    val watches: List<WatchInfo>? = null,
)

data class OkxPrice(val instrumentId: String, val price: Double? = null)

fun formatNumber(value: Double): String = value.toBigDecimal().stripTrailingZeros().toPlainString()
