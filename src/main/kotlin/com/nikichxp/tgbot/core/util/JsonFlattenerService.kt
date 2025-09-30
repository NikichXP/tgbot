package com.nikichxp.tgbot.core.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.DoubleNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.stereotype.Service

@Service
class JsonFlattenerService(
    private val objectMapper: ObjectMapper
) {

    fun toJsonAndFlatten(entity: Any): Map<String, Any> {
        val jsonNode = objectMapper.valueToTree<JsonNode>(entity)
        return if (jsonNode is ObjectNode) {
            flattenJson(jsonNode)
        } else {
            emptyMap()
        }
    }

    fun parseJsonAndFlatten(control: String): Map<String, Any> {
        val node = objectMapper.readTree(control)
        return if (node is ObjectNode) {
            flattenJson(node)
        } else {
            emptyMap()
        }
    }

    fun flattenJson(node: ObjectNode): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        flattenJson("", node, result)
        return result
    }

    private fun flattenJson(prefix: String, node: JsonNode, result: MutableMap<String, Any>) {
        when (node) {
            is ObjectNode -> {
                node.fieldNames().forEach {
                    val key = if (prefix.isEmpty()) it else "$prefix.$it"
                    val child = node.get(it)
                    flattenJson(key, child, result)
                }
            }
            is ArrayNode -> {
                for ((i, child) in node.elements().withIndex()) {
                    val key = "$prefix[$i]"
                    flattenJson(key, child, result)
                }
            }
            is IntNode -> {
                result[prefix] = node.asInt()
            }
            is DoubleNode -> {
                result[prefix] = node.asDouble()
            }
            else -> {
                result[prefix] = node.toString()
            }
        }
    }

}