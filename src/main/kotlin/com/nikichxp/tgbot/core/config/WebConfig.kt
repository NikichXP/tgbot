package com.nikichxp.tgbot.core.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import java.time.Duration

@Configuration
class WebConfig {
    @Bean
    fun corsConfiguration() = CorsWebFilter {
        CorsConfiguration().apply {
            allowedOrigins = listOf("*")
            allowedMethods = listOf("*")
            allowedHeaders = listOf("*")
            setMaxAge(Duration.ofDays(1))
        }
    }
}