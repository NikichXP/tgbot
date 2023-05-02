package com.nikichxp.tgbot.tooling

import com.nikichxp.tgbot.config.AppConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Service
class RawJsonLogger(
    private val mongoTemplate: MongoTemplate,
    appConfig: AppConfig
) {

    private var storingEnabled = appConfig.tracer.store
    private var indexTTL = appConfig.tracer.ttl

    @PostConstruct
    fun createIndexes() {
        mongoTemplate
            .indexOps(EventTrace::class.java)
            .ensureIndex(Index().on("time", Sort.Direction.ASC).expire(indexTTL, TimeUnit.HOURS))
    }

    fun logEvent(data: Document) = runBlocking {
        if (storingEnabled) {
            launch {
                mongoTemplate.save(EventTrace(data))
            }
        }
    }

}

data class EventTrace(val data: Document, val time: Instant = Instant.now())