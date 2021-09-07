package com.nikichxp.tgbot.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.wnameless.json.flattener.JsonFlattener
import com.nikichxp.tgbot.entity.UnparsedMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.stream
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct

@Profile("local")
@Component
class TestMessageParse(
    private val mongoTemplate: MongoTemplate,
    private val objectMapper: ObjectMapper
) {

    init {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @PostConstruct
    fun run() {
        var equal = 0
        var match = 0
        val diff = mutableListOf<Pair<String, String>>()
        var error = mutableListOf<String>()

        val file = File("export-${System.currentTimeMillis()}.json")
        file.createNewFile()
        val writer = file.writer()

        val jobs = mutableListOf<Job>()

        runBlocking {

            mongoTemplate.stream<UnparsedMessage>(Query()).forEach { message ->

                val source = message.content.toJson()
                try {
                    val update = objectMapper.readValue(source, Update::class.java)
                    val control = objectMapper.writeValueAsString(update)

                    val flatSrc = JsonFlattener.flattenAsMap(source)
                    val flatCtr = JsonFlattener.flattenAsMap(control)

                    if (source == control || flatSrc.keys == flatCtr.keys) {
                        writer.write(source + "\n")
                        jobs += launch {
                            mongoTemplate.save(update)
                            mongoTemplate.remove(message)
                        }
                    }
                } catch (e: Exception) {
                    error += source
                }
            }

            jobs.forEach { it.join() }
        }



        writer.close()

        println(
            """
            match: $match
            identical: $equal
            diffs: ${diff.map { "SRC: ${it.first}\nCTR: ${it.second}\n" }}
        """.trimIndent()
        )

    }
}