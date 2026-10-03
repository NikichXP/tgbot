package com.nikichxp.tgbot.mq

import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class HelloWorldListener {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @RabbitListener(queues = [HELLO_WORLD_QUEUE])
    fun onHelloWorld(payload: HelloWorldPayload) {
        log.info("Received hello-world message: $payload")
    }
}
