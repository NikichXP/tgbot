package com.nikichxp.tgbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class TGBotApplication

fun main(args: Array<String>) {
    runApplication<TGBotApplication>(*args)
}