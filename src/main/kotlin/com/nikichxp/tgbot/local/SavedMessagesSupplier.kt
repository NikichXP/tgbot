package com.nikichxp.tgbot.local

import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.util.function.Predicate
import javax.annotation.PostConstruct

@Profile("local")
@Service
class SavedMessagesSupplier(
    private val mongoTemplate: MongoTemplate
) {

    val filters: List<Predicate<Update>> = listOf()

    @PostConstruct
    fun startTest() {
        mongoTemplate.find<com.nikichxp.tgbot.dto.Update>(Query())
    }

}