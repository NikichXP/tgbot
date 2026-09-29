package com.nikichxp.tgbot.okx

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OkxWatchTest {

    @Test
    fun `parses percent and absolute thresholds`() {
        assertThat(Threshold.parse("2%")).isEqualTo(Threshold(ThresholdType.PERCENT, 2.0))
        assertThat(Threshold.parse("2,5%")).isEqualTo(Threshold(ThresholdType.PERCENT, 2.5))
        assertThat(Threshold.parse("850")).isEqualTo(Threshold(ThresholdType.ABSOLUTE, 850.0))
        assertThat(Threshold.parse("0")).isNull()
        assertThat(Threshold.parse("-1%")).isNull()
        assertThat(Threshold.parse("abc")).isNull()
    }

    @Test
    fun `formats triggered notification`() {
        val text = WatchNotificationFormatter.format(
            WatchNotification(
                type = WatchNotificationType.TRIGGERED,
                chatId = 1,
                botName = "bot",
                instrumentId = "BTC-USDT-SWAP",
                thresholdType = ThresholdType.PERCENT,
                thresholdValue = 2.0,
                direction = MoveDirection.DOWN,
                fromPrice = 100000.0,
                price = 98000.0,
                changePercent = -2.0,
            )
        )
        assertThat(text).isEqualTo("📉 BTC-USDT-SWAP: 100000 → 98000 (-2.00%, шаг 2%)")
    }
}
