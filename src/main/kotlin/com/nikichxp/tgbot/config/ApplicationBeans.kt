package com.nikichxp.tgbot.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nikichxp.tgbot.entity.UpdateContextHandler
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.annotation.RequestScope
import java.util.concurrent.Executors

@Configuration
class ApplicationBeans {

    @Bean
    fun restTemplate() = RestTemplateBuilder()
        .build()!!

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Bean
    fun coroutineDispatcher() = Executors.newCachedThreadPool().asCoroutineDispatcher()

    @Bean
    @Primary
    fun coroutineScope(coroutineDispatcher: CoroutineDispatcher) = CoroutineScope(coroutineDispatcher)

    @Bean
    fun ktorHttpClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            // TODO use bean from above
            jackson {
                registerModule(JavaTimeModule())
                registerKotlinModule()
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        engine {
            maxConnectionsCount = 1000
            requestTimeout = 60_000
            endpoint {
                maxConnectionsPerRoute = 100
                pipelineMaxSize = 20
                keepAliveTime = 5000
                connectTimeout = 5000
                connectAttempts = 5
            }
        }
    }
}
