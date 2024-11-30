package com.nikichxp.tgbot

import com.nikichxp.tgbot.core.config.AppConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableCaching
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(AppConfig::class)
class TGBotApplication

fun main(args: Array<String>) {
    runApplication<TGBotApplication>(*args)
}
