package com.nikichxp.tgbot.core.tooling

import com.nikichxp.tgbot.core.config.AppConfig
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.TimeUnit

@Service
class RawJsonLogger(
    private val mongoTemplate: MongoTemplate,
    private val coroutineDispatcher: CoroutineDispatcher,
    appConfig: AppConfig
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private var storingEnabled = appConfig.tracer.store
    private var indexTTL = appConfig.tracer.ttl

    @PostConstruct
    fun createIndexes() {
        logger.info("Logger status = $storingEnabled; TTL = $indexTTL")
        mongoTemplate
            .indexOps(EventTrace::class.java)
            .ensureIndex(Index().on("time", Sort.Direction.ASC).expire(indexTTL, TimeUnit.HOURS))
    }

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

data class EventTrace(val data: Document, val time: Instant = Instant.now())