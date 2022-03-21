package com.nikichxp.tgbot.works

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.nikichxp.tgbot.service.TextClassifier
import com.nikichxp.tgbot.service.TgOperations
import com.nikichxp.tgbot.service.UserInfo
import com.nikichxp.tgbot.service.actions.LikeReport
import com.nikichxp.tgbot.service.actions.LikeReportId
import com.nikichxp.tgbot.service.actions.LikedMessageService
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.stream
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.annotation.PostConstruct
import kotlin.concurrent.thread

@Profile("dev")
@Component
class RecalculateKarmaJob(
    private val mongoTemplate: MongoTemplate,
    private val textClassifier: TextClassifier,
    private val objectMapper: ObjectMapper,
    private val likedMessageService: LikedMessageService
) {

    fun recalculateNewKarma() {
        val karmaDb = mutableMapOf<Long, Double>()
        val reactedTo = mutableMapOf<Long, Int>()
        mongoTemplate.stream<LikeReport>(Query().with(Sort.by("date").ascending())).forEachRemaining {
            val actor = karmaDb[it.id.authorId] ?: 0.0
            val diff = LikedMessageService.calculateKarmaDiff(actor, it.power)
            val target = karmaDb[it.id.targetId] ?: 0.0
            karmaDb[it.id.targetId] = target + diff
            reactedTo[it.id.targetId] = (reactedTo[it.id.targetId] ?: 0) + 1
        }

        val endData = karmaDb.toList().sortedBy { -it.second }
            .map {
                (mongoTemplate.findById<UserInfo>(it.first)?.username ?: it.first) to (it.second to reactedTo[it.first])
            }
        println(endData)

        karmaDb.forEach { (id, rating) ->
            val user = mongoTemplate.findById<UserInfo>(id) ?: return@forEach
            user.rating = LikedMessageService.roundF(rating)
            mongoTemplate.save(user)
        }
        println()
    }

    // exportFile == "./messages.json"
    @Suppress("unused")
    fun importActionsFromChatHistoryExport(exportFile: String) {

        data class Snatch(
            val message: JsonNode,
            val rating: Double,
            var reply: JsonNode? = null
        )

        val messages = objectMapper.readTree(File(exportFile).readText())["messages"].asIterable() as ArrayNode
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
                do {
                    Thread.sleep(1_000)
                    println("Updated $ctr/${jobs.size}")
                } while (jobs.any { it.isActive })
            }
            jobs.forEach {
                it.join()
            }
        }
    }

}