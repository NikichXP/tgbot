package com.nikichxp.tgbot.mq

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.Queue
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
    fun rabbitMessageConverter(objectMapper: ObjectMapper): MessageConverter = Jackson2JsonMessageConverter(objectMapper)
}
