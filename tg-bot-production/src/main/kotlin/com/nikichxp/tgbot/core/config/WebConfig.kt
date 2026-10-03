package com.nikichxp.tgbot.core.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import java.time.Duration

@Configuration
class WebConfig(
    private val appConfig: AppConfig
) {

    private val publicCors = CorsConfiguration().apply {
        allowedOrigins = listOf("*")
        allowedMethods = listOf("*")
        allowedHeaders = listOf("*")
        setMaxAge(Duration.ofDays(1))
    }

    private val dashboardCorsWithCredentials by lazy {
        CorsConfiguration().apply {
            allowedOrigins = appConfig.dashboard.allowedOrigins.map { it.trim() }.filter { it.isNotEmpty() }
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type")
            allowCredentials = true
            setMaxAge(Duration.ofDays(1))
        }
    }

    @Bean
    fun corsConfiguration() = CorsWebFilter { exchange ->
        if (exchange.request.path.value().startsWith("/admin")) dashboardCorsWithCredentials else publicCors
    }
}
