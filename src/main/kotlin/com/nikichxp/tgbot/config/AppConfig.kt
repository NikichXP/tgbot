package com.nikichxp.tgbot.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Value("\${OWNER_ID:0}")
    final var ownerId: Long = 0
    @Value("\${HEROKU_APP_NAME:}")
    final var appName: String = ""
    @Value("\${HEROKU_RELEASE_VERSION:}")
    final var appVersion: String = ""

    @Bean
    fun restTemplate() = RestTemplateBuilder()
        .build()!!

    @Bean
    fun objectMapper() = ObjectMapper()
        .registerKotlinModule()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

}