package com.nikichxp.tgbot.okx.config

import com.nikichxp.tgbot.okx.OKX_WATCH_COMMANDS_QUEUE
import com.nikichxp.tgbot.okx.OKX_WATCH_NOTIFICATIONS_QUEUE
import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OkxRabbitConfig {

    @Bean
    fun okxWatchCommandsQueue(): Queue = Queue(OKX_WATCH_COMMANDS_QUEUE, true)

    @Bean
    fun okxWatchNotificationsQueue(): Queue = Queue(OKX_WATCH_NOTIFICATIONS_QUEUE, true)
}
