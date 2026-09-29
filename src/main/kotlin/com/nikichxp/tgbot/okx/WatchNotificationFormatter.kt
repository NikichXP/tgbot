package com.nikichxp.tgbot.okx

import java.util.Locale

object WatchNotificationFormatter {

    fun format(n: WatchNotification): String {
        val threshold = threshold(n.thresholdType, n.thresholdValue)
        return when (n.type) {
            WatchNotificationType.REGISTERED ->
                "Слежу за ${n.instrumentId}: шаг $threshold" + (n.price?.let { ", сейчас ${formatNumber(it)}" } ?: "")

            WatchNotificationType.UNREGISTERED -> "Больше не слежу за ${n.instrumentId}"

            WatchNotificationType.TRIGGERED -> {
                val arrow = if (n.direction == MoveDirection.UP) "📈" else "📉"
                val change = n.changePercent?.let { String.format(Locale.US, "%+.2f%%", it) } ?: ""
                "$arrow ${n.instrumentId}: ${n.fromPrice?.let(::formatNumber)} → ${n.price?.let(::formatNumber)} ($change, шаг $threshold)"
            }

            WatchNotificationType.LIST -> {
                val watches = n.watches.orEmpty()
                if (watches.isEmpty()) {
                    "Нет активных отслеживаний"
                } else {
                    "Отслеживания:\n" + watches.joinToString("\n") {
                        "${it.instrumentId}: шаг ${threshold(it.thresholdType, it.thresholdValue)}" +
                            (it.price?.let { p -> ", сейчас ${formatNumber(p)}" } ?: "")
                    }
                }
            }

            WatchNotificationType.ERROR -> "Ошибка${n.instrumentId?.let { " ($it)" } ?: ""}: ${n.message}"
        }
    }

    private fun threshold(type: ThresholdType?, value: Double?): String =
        if (type != null && value != null) Threshold(type, value).toString() else "?"
}
