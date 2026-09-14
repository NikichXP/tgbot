package com.nikichxp.tgbot.core.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JsonFlattenerServiceTest {

    private val service = JsonFlattenerService(ObjectMapper())

    @Test
    fun `extracts flat leaf paths`() {
        val json = """{"foo": 123, "baz": "str", "flag": true, "nothing": null}"""

        assertThat(service.extractLeafPaths(json))
            .containsExactlyInAnyOrder("foo", "baz", "flag", "nothing")
    }

    @Test
    fun `extracts nested object paths`() {
        val json = """{"a": {"b": {"c": 1}, "d": 2}}"""

        assertThat(service.extractLeafPaths(json))
            .containsExactlyInAnyOrder("a.b.c", "a.d")
    }

    @Test
    fun `array elements use bracket notation`() {
        val json = """{"foo": 123, "bar": [{"x": 456}]}"""

        assertThat(service.extractLeafPaths(json))
            .containsExactlyInAnyOrder("foo", "bar[].x")
    }

    @Test
    fun `array of scalars produces array path`() {
        val json = """{"arr": [1, 2, 3]}"""

        assertThat(service.extractLeafPaths(json))
            .containsExactlyInAnyOrder("arr[]")
    }

    @Test
    fun `multiple array elements are deduplicated`() {
        val json = """{"bar": [{"x": 1}, {"x": 2, "y": 3}]}"""

        assertThat(service.extractLeafPaths(json))
            .containsExactlyInAnyOrder("bar[].x", "bar[].y")
    }

    @Test
    fun `nested arrays produce repeated brackets`() {
        val json = """{"a": [[1, 2], [{"b": 3}]]}"""

        assertThat(service.extractLeafPaths(json))
            .containsExactlyInAnyOrder("a[][]", "a[][].b")
    }

    @Test
    fun `empty object is recorded as its own path`() {
        val json = """{"a": {}, "b": 1}"""

        assertThat(service.extractLeafPaths(json))
            .containsExactlyInAnyOrder("a", "b")
    }

    @Test
    fun `empty array is recorded with brackets`() {
        val json = """{"a": []}"""

        assertThat(service.extractLeafPaths(json))
            .containsExactlyInAnyOrder("a[]")
    }

    @Test
    fun `empty root object produces no paths`() {
        assertThat(service.extractLeafPaths("""{}""")).isEmpty()
    }

    @Test
    fun `non-object root produces no paths`() {
        assertThat(service.extractLeafPaths("""[1, 2]""")).isEmpty()
        assertThat(service.extractLeafPaths("""42""")).isEmpty()
    }

    @Test
    fun `realistic telegram update`() {
        val json = """
            {
              "update_id": 123,
              "message": {
                "message_id": 5,
                "from": {"id": 42, "first_name": "John"},
                "chat": {"id": 42, "type": "private"},
                "text": "hello",
                "entities": [{"type": "bold", "offset": 0, "length": 2}]
              }
            }
        """.trimIndent()

        assertThat(service.extractLeafPaths(json)).containsExactlyInAnyOrder(
            "update_id",
            "message.message_id",
            "message.from.id",
            "message.from.first_name",
            "message.chat.id",
            "message.chat.type",
            "message.text",
            "message.entities[].type",
            "message.entities[].offset",
            "message.entities[].length"
        )
    }
}
