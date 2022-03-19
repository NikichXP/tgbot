package com.nikichxp.tgbot

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.nikichxp.tgbot.service.TextClassifier
import com.nikichxp.tgbot.service.TgOperations
import com.nikichxp.tgbot.service.actions.LikeReport
import com.nikichxp.tgbot.service.actions.LikeReportId
import com.nikichxp.tgbot.service.actions.LikedMessageService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.annotation.PostConstruct
import kotlin.concurrent.thread

//@Component
class RecalculateKarmaJob(
    private val mongoTemplate: MongoTemplate,
    private val textClassifier: TextClassifier,
    private val tgOperations: TgOperations,
    private val objectMapper: ObjectMapper
) {

//    @PostConstruct
    fun work() {

        data class Snatch(
            val message: JsonNode,
            val rating: Double,
            var reply: JsonNode? = null
        )

        val messages = objectMapper.readTree(File("./messages.json").readText())["messages"].asIterable() as ArrayNode
        val messageDb = messages.associateBy { it["id"].asLong() }
        val relatedMessages = messages
            .asSequence()
            .filter { it["text"] != null && it["reply_to_message_id"] != null }
            .map { Snatch(it, textClassifier.getReaction(it["text"].asText())) }
            .filter { it.rating != 0.0 }
            .filter {
                it.reply = messageDb[it.message["reply_to_message_id"].asLong()]
                it.reply != null
            }.map {
                try {
                    LikeReport(
                        id = LikeReportId(
                            authorId = it.message["from_id"].asText().substringAfter("user").toLong(),
                            targetId = it.reply!!.get("from_id").asText().substringAfter("user").toLong(),
                            messageId = it.message["id"].asLong()
                        ),
                        power = it.rating,
                        date = LocalDateTime.parse(it.message["date"].asText()).toEpochSecond(ZoneOffset.UTC),
                        source = "export"
                    )
                } catch (e: Exception) {
                    null
                }
            }.filterNotNull()
            .toList()

        runBlocking {
            var ctr = 0
            val jobs = relatedMessages.map {
                launch {
                    val existing = mongoTemplate.findById<LikeReport>(it.id)
                    if (existing != null && existing.power == null) {
                        existing.power = it.power
                        if (existing.source == null) {
                            existing.source = "update"
                        }
                        mongoTemplate.save(existing)
                    } else {
                        mongoTemplate.save(it)
                    }
                    ctr++
                }
            }
            thread(start = true) {
                while (jobs.any { it.isActive }) {
                    println("Updated $ctr/${jobs.size}")
                    Thread.sleep(1_000)
                }
            }
            jobs.forEach {
                it.join()
            }
        }

        val karmaPoints = mutableMapOf<Long, Double>()
        mongoTemplate.findAll<LikeReport>().forEach {
            val actorKarma = karmaPoints[it.id.authorId] ?: 0.0
            val targetKarma = karmaPoints[it.id.targetId] ?: 0.0
//            LikedMessageService.calculateKarmaDiff(actorKarma, )
            println(it)
        }
    }

}