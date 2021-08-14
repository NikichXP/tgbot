package com.nikichxp.tgbot.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean
    fun restTemplate() = RestTemplateBuilder()
            .build()!!

}