package com.nikichxp.tgbot.tooling

import com.nikichxp.tgbot.config.AppConfig
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
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
    private val dispatcher: CoroutineScope,
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

    fun logEvent(data: Document) {
        if (storingEnabled) {
            val trace = EventTrace(data)
            dispatcher.launch {
                try {
                    logger.info("start saving trace, id = ${trace.id}")
                    mongoTemplate.insert(trace)
                } catch (e: Exception) {
                    logger.error("Failed to save trace", e)
                }
            }
        }
    }
}

data class EventTrace(val data: Document, val time: Instant = Instant.now()) {
    @Id
    var id: String = data["_id"] as String
}