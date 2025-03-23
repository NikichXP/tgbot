package com.nikichxp.tgbot.core.service.helper

import com.nikichxp.tgbot.core.entity.StoredError
import org.slf4j.Logger
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service

@Service
class ErrorService(
    private val mongoTemplate: MongoTemplate
) {

    fun logAndReportError(logger: Logger, message: String, data: Any = Unit) {
        logger.error(message)
        reportError(message, data)
    }

    fun reportError(message: String, data: Any = Unit) {
        val storedError = StoredError(message, data)
        mongoTemplate.save(storedError)
    }

}