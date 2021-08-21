package com.nikichxp.tgbot.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.wnameless.json.flattener.JsonFlattener
import com.nikichxp.tgbot.entity.UnparsedMessage
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.stream
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class MessageAnalyzer(
    private val objectMapper: ObjectMapper,
    private val mongoTemplate: MongoTemplate,
    private val messageClassifier: MessageClassifier
) {

    /*
    важно:
    - если отредактировать сообщение, то message - не будет, будет edited_message

     */

    val entriesMap = mutableMapOf<String, Int>()

    @PostConstruct
    fun statistics() {
        mongoTemplate.stream<UnparsedMessage>(Query()).forEachRemaining {
            val map = JsonFlattener.flattenAsMap(it.content.toJson())
            map.keys.forEach { key -> entriesMap[key] = (entriesMap[key] ?: 0) + 1 }
            messageClassifier.getMessageHandler(it.content)
        }
//        entriesMap.forEach { key, count -> println("---- $key\t\t$count") }
        println()
    }

    fun startMessageParse(json: String) {
        val message: TgMessage = objectMapper.readValue(json)
    }

}

data class TgMessage(val update_id: Long)