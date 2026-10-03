package com.nikichxp.tgbot.mq

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.okx.OKX_WATCH_COMMANDS_QUEUE
import com.nikichxp.tgbot.okx.OKX_WATCH_NOTIFICATIONS_QUEUE
import org.springframework.amqp.core.Queue
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val HELLO_WORLD_QUEUE = "hello-world"

@Configuration
class RabbitConfig {

    @Bean
    fun helloWorldQueue(): Queue = Queue(HELLO_WORLD_QUEUE)

    @Bean
    fun okxWatchCommandsQueue(): Queue = Queue(OKX_WATCH_COMMANDS_QUEUE, true)

    @Bean
    fun okxWatchNotificationsQueue(): Queue = Queue(OKX_WATCH_NOTIFICATIONS_QUEUE, true)

    @Bean
    fun rabbitMessageConverter(objectMapper: ObjectMapper): MessageConverter =
        Jackson2JsonMessageConverter(objectMapper).apply {
            javaTypeMapper = DefaultJackson2JavaTypeMapper().apply {
                typePrecedence = Jackson2JavaTypeMapper.TypePrecedence.INFERRED
                setTrustedPackages("*")
            }
        }
}
