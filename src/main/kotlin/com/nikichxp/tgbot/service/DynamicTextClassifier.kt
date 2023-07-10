package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.service.classifier.ClassifierInt
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.data.mongodb.core.stream
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class DynamicTextClassifier(
    private val mongoTemplate: MongoTemplate
) : ClassifierInt {

    private val defined = mutableMapOf<String, Double>()
    private val prefixes = mutableMapOf<String, Double>()

    @PostConstruct
    fun init() {
        mongoTemplate
            .stream<TextEntry>(Query())
            .forEachRemaining(::loadEntry)
    }

    override fun classify(input: String): Double {
        val trimText = input.trim().lowercase()
        defined.forEach { (key, value) ->
            if (trimText == key) {
                return value
            }
        }
        prefixes.forEach { (key, value) ->
            if (trimText.startsWith(key)) {
                return value
            }
        }
        return when {
            trimText.startsWith('+') -> 1.0
            trimText.startsWith('-') -> -1.0
            else -> 0.0
        }
    }

    // TODO connect it to something
    fun addEntry(text: String, rating: Double, isDefined: Boolean) {
        TextEntry(text, rating, isDefined).also {
            mongoTemplate.save(it)
            loadEntry(it)
        }
    }

    // TODO connect it to something
    fun deleteEntry(text: String?, id: String?) {
        val query = Query()
        if (text != null) {
            query.addCriteria(Criteria.where(TextEntry::text.name).`is`(text))
        } else if (id != null) {
            query.addCriteria(Criteria.where(TextEntry::id.name).`is`(id))
        } else {
            throw IllegalArgumentException("Both text and id are null")
        }
        mongoTemplate.remove<TextEntry>(query)
    }

    private fun loadEntry(entry: TextEntry) {
        if (entry.isDefined) {
            defined[entry.text] = entry.rating
        } else {
            prefixes[entry.text] = entry.rating
        }
    }
}

data class TextEntry(
    val text: String,
    val rating: Double,
    val isDefined: Boolean
) {

    @Id
    @Field(targetType = FieldType.OBJECT_ID)
    lateinit var id: String

}