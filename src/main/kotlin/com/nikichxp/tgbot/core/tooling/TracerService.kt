package com.nikichxp.tgbot.core.tooling

import com.nikichxp.tgbot.core.config.AppConfig
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class TracerService(
    private val mongoTemplate: MongoTemplate,
    private val coroutineDispatcher: CoroutineDispatcher,
    appConfig: AppConfig
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private var storingEnabled = appConfig.tracer.store
    private var entriesTTL = appConfig.tracer.ttl
    private var capacity = appConfig.tracer.capacity

    @PostConstruct
    fun createIndexes() {
        logger.info("Logger status = $storingEnabled; TTL = $entriesTTL/capacity = $capacity")
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    fun removeOutdated() {
        // nothing here so far
    }

    fun list() = mongoTemplate.findAll<EventTrace>().sortedByDescending { it.time }

    suspend fun logEvent(data: Document) = coroutineScope {
        if (storingEnabled) {
            val trace = EventTrace(data)
            launch(coroutineDispatcher) {
                try {
                    mongoTemplate.insert(trace)
                } catch (e: Exception) {
                    logger.error("Failed to save trace", e)
                }
            }
        }
    }
}
