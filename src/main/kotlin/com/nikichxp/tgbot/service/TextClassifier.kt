package com.nikichxp.tgbot.service

import org.springframework.stereotype.Service
import org.yaml.snakeyaml.Yaml
import java.io.File

@Service
class TextClassifier {

    private val positive: List<String>
    private val negative: List<String>

    init {
        val content: String = try {
            File(System.getProperty("user.dir") + "/src/main/resources/dictionary.yawml").readText()
        } catch (e: Exception) {
            ""
        }

        val yaml = Yaml()
        val obj = yaml.load<Map<String, List<String>>>(content)
        positive = obj["positive"] ?: listOf()
        negative = obj["negative"] ?: listOf()
    }

    fun getReaction(text: String): Double {
        return when {
            text.trim().startsWith("+ ") -> 1.0
            text.trim().startsWith("- ") -> -1.0
            else -> 0.0
        }
    }
}