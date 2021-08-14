package com.nikichxp.tgbot.data

import com.nikichxp.tgbot.entity.UnparsedMessage
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.indexOps
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class UnparsedMessagesDAO(
        private val mongoTemplate: MongoTemplate
) {

    @PostConstruct
    fun init() {
        mongoTemplate.indexOps<UnparsedMessage>().ensureIndex(Index().on("created", Sort.Direction.ASC))
    }

}