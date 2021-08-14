package com.nikichxp.tgbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TGBotApplication

fun main(args: Array<String>) {
    runApplication<TGBotApplication>(*args)
}
