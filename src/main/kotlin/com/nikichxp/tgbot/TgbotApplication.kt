package com.nikichxp.tgbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TgbotApplication

fun main(args: Array<String>) {
	runApplication<TgbotApplication>(*args)
}
