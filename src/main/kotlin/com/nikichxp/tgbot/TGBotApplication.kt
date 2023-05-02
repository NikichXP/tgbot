package com.nikichxp.tgbot

import com.nikichxp.tgbot.config.AppConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(AppConfig::class)
class TGBotApplication

fun main(args: Array<String>) {
    runApplication<TGBotApplication>(*args)
}