package com.nikichxp.tgbot.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Value("\${HEROKU_APP_NAME:}")
    final var appName: String = ""
    @Value("\${HEROKU_RELEASE_VERSION:}")
    final var appVersion: String = ""

    @Bean
    fun restTemplate() = RestTemplateBuilder()
        .build()!!

}